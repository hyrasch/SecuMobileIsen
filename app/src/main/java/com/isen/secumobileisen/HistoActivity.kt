package com.isen.secumobileisen

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class HistoActivity : AppCompatActivity() {

    lateinit var recycler_view: RecyclerView
    private var adapter: ProductFirestoreRecyclerAdapter? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_histo)


        val query = db!!.collection("histopatients").orderBy("name", Query.Direction.ASCENDING)

        recycler_view = findViewById(R.id.histoView)
        recycler_view.layoutManager = LinearLayoutManager(this)
        val options = FirestoreRecyclerOptions.Builder<Patients>().setQuery(query, Patients::class.java).build()

        adapter = ProductFirestoreRecyclerAdapter(options)
        recycler_view.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()

        if (adapter != null) {
            adapter!!.stopListening()
        }
    }


    private inner class ProductViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {
        internal fun setPatientName(Name: String) {
            val patientName = view.findViewById<TextView>(R.id.histoName)
            patientName.text = Name
        }
        internal fun setPatientDate(Date: String) {
            val patientDate = view.findViewById<TextView>(R.id.histoDate)
            patientDate.text = Date
        }
        internal fun setPatientPathology(Pathology: String) {
            val patientPathology = view.findViewById<TextView>(R.id.histoPathology)
            patientPathology.text = Pathology
        }
        internal fun setPatientTreatments(Treatments: String) {
            val patientTreatments = view.findViewById<TextView>(R.id.histoTreatments)
            patientTreatments.text = Treatments
        }
        internal fun setPatientToday(Today: String) {
            val patientToday = view.findViewById<TextView>(R.id.histoToday)
            patientToday.text = Today
        }
    }

    private inner class ProductFirestoreRecyclerAdapter internal constructor(options: FirestoreRecyclerOptions<Patients>) : FirestoreRecyclerAdapter<Patients, ProductViewHolder>(options) {
        override fun onBindViewHolder(productViewHolder: ProductViewHolder, position: Int, patients: Patients) {
            var name = decrypt(patients.name)
            var patho = decrypt(patients.pathology)
            var traitement = decrypt(patients.treatments)
            var description = decrypt(patients.today)
            var dateVisite = decrypt(patients.date)

            productViewHolder.setPatientName(name.toString())
            productViewHolder.setPatientDate(patho.toString())
            productViewHolder.setPatientToday(traitement.toString())
            productViewHolder.setPatientPathology(description.toString())
            productViewHolder.setPatientTreatments(dateVisite.toString())
            //productViewHolder.setPatientImage(patients.image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.histo_layout, parent, false)

            return ProductViewHolder(view)
        }
    }

    fun encrypt(strToEncrypt: String, secret: String): String? {
        try {
            var key: ByteArray
            key = secret.toByteArray()
            key = Arrays.copyOf(key, 16)
            var secretKey = SecretKeySpec(key, "AES")
            var iv = "jdetestelekotlin"
            val ivParam = IvParameterSpec(iv.toByteArray())

            val cipher =
                Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParam)
            return Base64.getEncoder()
                .encodeToString(cipher.doFinal(strToEncrypt.toByteArray(charset("UTF-8"))))
        } catch (e: Exception) {
            println("Error while encrypting: $e")
        }
        return null
    }

    fun decrypt(strToDecrypt: String?): String? {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKey = (keyStore.getEntry(
                "venotbg",
                null
            ) as KeyStore.SecretKeyEntry).secretKey

            val Iv = "jdetestelekotlin"
            val IvParameterSpec = IvParameterSpec(Iv.toByteArray())

            val db = getSharedPreferences("user_db", Activity.MODE_PRIVATE)
            val doc_alias = "alias" + strToDecrypt
            val iv = db.getString(doc_alias,"samarshpas").toString()
            val ivParameterSpec = IvParameterSpec(iv.toByteArray())
            Log.d("Alias", doc_alias)
            Log.d("iv", iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec)
            return String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))
        } catch (e: java.lang.Exception) {
            println("Error while decrypting: $e")
        }
        return null
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
