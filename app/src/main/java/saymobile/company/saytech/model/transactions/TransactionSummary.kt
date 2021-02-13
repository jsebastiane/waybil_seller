package saymobile.company.saytech.model.transactions

import com.google.firebase.firestore.IgnoreExtraProperties
import java.sql.Timestamp
@IgnoreExtraProperties
data class TransactionSummary(
    val week1: HashMap<String?, Any?> = hashMapOf(),
    val week2: HashMap<String?, Any?> = hashMapOf(),
    val week3: HashMap<String?, Any?> = hashMapOf(),
    val week4: HashMap<String?, Any?> = hashMapOf(),
    val week5: HashMap<String?, Any?> = hashMapOf()
)