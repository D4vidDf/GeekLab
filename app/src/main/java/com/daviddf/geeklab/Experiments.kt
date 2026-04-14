package com.daviddf.geeklab

import com.google.firebase.firestore.PropertyName

data class Experiments(
    @get:PropertyName("Imagen") @set:PropertyName("Imagen") var imagen: String? = null,
    @get:PropertyName("Titulo") @set:PropertyName("Titulo") var titulo: String? = null,
    @get:PropertyName("Url") @set:PropertyName("Url") var url: String? = null,
    @get:PropertyName("Tag") @set:PropertyName("Tag") var tag: String? = null
)
