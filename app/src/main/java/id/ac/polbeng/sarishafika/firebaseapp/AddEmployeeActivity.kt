package id.ac.polbeng.sarishafika.firebaseapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import id.ac.polbeng.sarishafika.firebaseapp.databinding.ActivityAddEmployeeBinding
import org.valiktor.ConstraintViolationException

class AddEmployeeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEmployeeBinding
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("Employees")

        binding.btnSave.setOnClickListener {
            saveEmployeeData()
        }
    }

    private fun saveEmployeeData() {
        val empName = binding.etName.text.toString().trim()
        val empEmail = binding.etEmail.text.toString().trim()
        val empAge = binding.etAge.text.toString().trim()
        val empSalary = binding.etSalary.text.toString().trim()

        var error = false

        if (empName.isEmpty()) {
            binding.etName.error = "Nama tidak boleh kosong!"
            error = true
        }

        if (empEmail.isEmpty()) {
            binding.etEmail.error = "Email tidak boleh kosong!"
            error = true
        }

        if (empAge.isEmpty()) {
            binding.etAge.error = "Umur tidak boleh kosong!"
            error = true
        }

        if (empSalary.isEmpty()) {
            binding.etSalary.error = "Pendapatan tidak boleh kosong!"
            error = true
        }

        if (error) return

        val intAge: Int
        val longSalary: Long

        try {
            intAge = empAge.toInt()
            longSalary = empSalary.toLong()
        } catch (_: NumberFormatException) {
            Toast.makeText(this, "Inputan umur atau pendapatan salah!", Toast.LENGTH_SHORT).show()
            return
        }

        val empID = dbRef.push().key!!
        val employee: EmployeeModel

        try {
            employee = EmployeeModel(empID, empName, empEmail, intAge, longSalary)
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
            return
        }

        dbRef.child(empID).setValue(employee)
            .addOnCompleteListener {
                Toast.makeText(this, "Data inserted successfully", Toast.LENGTH_SHORT).show()
                binding.etName.text.clear()
                binding.etEmail.text.clear()
                binding.etAge.text.clear()
                binding.etSalary.text.clear()
            }
            .addOnFailureListener { err ->
                Toast.makeText(this, "Error : ${err.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showFullTextToast(context: Context, message: String) {
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
}