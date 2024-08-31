package com.vaultmessenger.demoData

import com.vaultmessenger.R
import com.vaultmessenger.model.Contact

class ContactListsData {
    val contactProfilePhoto = listOf(
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground,
        R.drawable.ic_account_circle_foreground
    )

    val contactName = listOf(
        "John",
        "Emily",
        "Michael",
        "Sarah",
        "David",
        "Jessica"
    )

    val contactNumber = listOf(
        "+123456789",
        "+987654321",
        "+192837465",
        "+564738291",
        "+374829102",
        "+918273645"
    )
   // fun getContactList(): Contact {
        //return Contact(contactProfilePhoto, contactName, contactNumber)
    //}
}
