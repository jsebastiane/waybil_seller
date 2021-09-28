package waybilmobile.company.waybil.model.user

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Name (
    val firstName: String = "",
    val lastName: String = ""
)