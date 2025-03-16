package com.example.framereality

import android.net.Uri

class ModelImagePicked {

    var id = ""
    var localImageUri: Uri? = null  // Renamed to differentiate between Uri and String
    var internetImageUri: String? = null
    var isFromInternet: Boolean = false

    constructor()

    constructor(id: String, localImageUri: Uri?, internetImageUri: String?, isFromInternet: Boolean) {
        this.id = id
        this.localImageUri = localImageUri
        this.internetImageUri = internetImageUri
        this.isFromInternet = isFromInternet
    }
}
