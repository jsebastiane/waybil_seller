package waybilmobile.company.waybil.view.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_invite_client.*
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.util.getProgressDrawable
import waybilmobile.company.waybil.util.loadImage


class InviteClient : Fragment() {

    private var currentUser = FirebaseAuth.getInstance().currentUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_invite_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_inviteClient.setOnClickListener {
            findNavController().navigateUp()
        }

        seller_qr.apply {
            clipToOutline = true
        }

        getUserQr()

    }

    private fun getUserQr(){
        currentUser?.let {
            val qrReference = FirebaseStorage.getInstance().reference
                .child("/${it.uid}/qr/qr.jpeg")

            seller_qr.loadImage(qrReference, getProgressDrawable(seller_qr.context))



        }

    }
}