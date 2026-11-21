package id.ac.polbeng.sarishafika.firebaseapp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import id.ac.polbeng.sarishafika.firebaseapp.databinding.ActivityEmployeeDetailBinding
import id.ac.polbeng.sarishafika.firebaseapp.databinding.EmployeeDialogBinding
import org.valiktor.ConstraintViolationException

class EmployeeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeDetailBinding
    private lateinit var employee: EmployeeModel
    private lateinit var dbRef: DatabaseReference
    private lateinit var employeeDialogBinding: EmployeeDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("Employees")

        val json = intent.getStringExtra(MainActivity.EXTRA_EMPLOYEE)
        employee = Gson().fromJson(json, EmployeeModel::class.java)

        Glide.with(applicationContext)
            .load(R.drawable.photo)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.imgItemPhoto)

        binding.tvId.text = employee.empID
        binding.tvName.text = employee.empName
        binding.tvEmail.text = employee.empEmail
        binding.tvAge.text = employee.empAge.toString()
        binding.tvSalary.text = employee.empSalary.toString()

        binding.btnUpdate.setOnClickListener {
            openUpdateDialog(employee.empID!!, employee.empName!!)
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Data")
                .setMessage("Data yang dihapus tidak dapat dikembalikan. Lanjutkan?")
                .setPositiveButton("Ya, Hapus") { dialog, _ ->
                    deleteEmployee(employee.empID!!)
                    dialog.dismiss()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun openUpdateDialog(empID: String, empName: String) {
        val employeeDialog = AlertDialog.Builder(this)
        employeeDialogBinding = EmployeeDialogBinding.inflate(layoutInflater)

        employeeDialog.setView(employeeDialogBinding.root)

        employeeDialogBinding.etEmpName.setText(employee.empName)
        employeeDialogBinding.etEmpEmail.setText(employee.empEmail)
        employeeDialogBinding.etEmpAge.setText(employee.empAge.toString())
        employeeDialogBinding.etEmpSalary.setText(employee.empSalary.toString())

        employeeDialog.setTitle("Ubah Data: $empName")

        val alertDialog = employeeDialog.create()
        alertDialog.show()

        employeeDialogBinding.btnSave.setOnClickListener {
            val name = employeeDialogBinding.etEmpName.text.toString().trim()
            val email = employeeDialogBinding.etEmpEmail.text.toString().trim()
            val age = employeeDialogBinding.etEmpAge.text.toString().trim()
            val salary = employeeDialogBinding.etEmpSalary.text.toString().trim()

            var error = false
            if (name.isEmpty()) {
                employeeDialogBinding.etEmpName.error = "Nama tidak boleh kosong!"
                error = true
            }
            if (email.isEmpty()) {
                employeeDialogBinding.etEmpEmail.error = "Email tidak boleh kosong!"
                error = true
            }
            if (age.isEmpty()) {
                employeeDialogBinding.etEmpAge.error = "Umur tidak boleh kosong!"
                error = true
            }
            if (salary.isEmpty()) {
                employeeDialogBinding.etEmpSalary.error = "Pendapatan tidak boleh kosong!"
                error = true
            }
            if (error) return@setOnClickListener

            val intAge: Int
            val longSalary: Long
            try {
                intAge = age.toInt()
                longSalary = salary.toLong()
            } catch (_: NumberFormatException) {
                Toast.makeText(this, "Inputan umur atau pendapatan salah!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val employeeUpdate: EmployeeModel
            try {
                employeeUpdate = EmployeeModel(empID, name, email, intAge, longSalary)
                updateEmployeeData(empID, employeeUpdate)

                employee = employeeUpdate
                binding.tvId.text = empID
                binding.tvName.text = name
                binding.tvEmail.text = email
                binding.tvAge.text = age
                binding.tvSalary.text = salary

                alertDialog.dismiss()
            } catch (ex: ConstraintViolationException) {
                val message = ex.constraintViolations.joinToString("\n") { violation ->
                    val fieldLabel = when (violation.property) {
                        "empName" -> "Nama"
                        "empEmail" -> "Email"
                        "empAge" -> "Umur"
                        "empSalary" -> "Pendapatan"
                        else -> violation.property
                    }
                    val detailPesan = when (violation.constraint.name) {
                        "Size" -> "Panjang karakter harus antara 3 sampai 30"
                        "Email" -> "Format email tidak valid"
                        "Between" -> "Nilai harus di antara 17 sampai 55"
                        "Greater" -> "Nilai harus lebih besar dari 0"
                        "IntegerDigits" -> "Jumlah digit angka harus antara 6 sampai 10"
                        else -> "Data tidak sesuai ketentuan"
                    }
                    "$fieldLabel: $detailPesan"
                }
                showFullTextToast(this, message)
                return@setOnClickListener
            }
        }
    }

    private fun updateEmployeeData(id: String, employeeUpdate: EmployeeModel) {
        dbRef.child(id).setValue(employeeUpdate)
            .addOnSuccessListener {
                Toast.makeText(this, "Data updated succesfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { err ->
                Toast.makeText(this, "Error : ${err.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun showFullTextToast(context: Context, message: String) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(context)
        val toastView: View = inflater.inflate(R.layout.custom_toast, null)

        // Set the text
        val toastText = toastView.findViewById<TextView>(R.id.toast_text)
        toastText.text = message

        // Create and show the Toast
        val toast = Toast(context)
        toast.view = toastView
        toast.duration = Toast.LENGTH_LONG // Use LENGTH_LONG (3.5s) for longer texts
        toast.show()
    }

    private fun deleteEmployee(id: String) {
        dbRef.child(id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Data delete succesfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { err ->
                Toast.makeText(this, "Error : ${err.message}", Toast.LENGTH_SHORT).show()
            }
    }
}