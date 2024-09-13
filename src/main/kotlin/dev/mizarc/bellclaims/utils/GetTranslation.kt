package dev.mizarc.bellclaims.utils

import dev.mizarc.bellclaims.BellClaims

//put "import dev.mizarc.bellclaims.utils.getLangText" in other files, 
//and use getLangText("key") to get <key: "text"> from lang_XX.yml file

fun getLangText(key: String): String {
    return BellClaims.instance.getText(key)
}