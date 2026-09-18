package com.tabboz.simulator.win

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Un controllo definito nel file .rc (coordinate in dialog units, 1 DLU = 2 pixel logici). */
data class ControlTemplate(
    val text: String,
    val id: Int,
    val cls: String,          // STATIC, BUTTON, EDIT, COMBOBOX, BorShade, BorBtn, BorStatic, BorRadio, BorCheck, BMPView, BMPTipa, msctls_progress
    val styles: Set<String>,  // es. BS_PUSHBUTTON, SS_CENTER, WS_BORDER, WS_DISABLED ...
    val styleNum: Int,        // parte numerica dello stile (es. 0x8001 per BorShade, 0x9 per BorRadio)
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
) {
    fun has(style: String) = style in styles
}

/** Un dialogo definito nel file .rc. */
data class DialogTemplate(
    val id: Int,
    val w: Int,               // larghezza area client in DLU
    val h: Int,               // altezza area client in DLU
    val caption: String,
    val styles: Set<String>,
    val menu: String?,        // nome del menu (solo la finestra principale)
    val controls: List<ControlTemplate>, // nell'ordine del file .rc (il primo e' in cima allo z-order)
) {
    fun control(id: Int): ControlTemplate? = controls.firstOrNull { it.id == id }
    fun has(style: String) = style in styles
}

object DialogTemplates {
    fun load(context: Context): Map<Int, DialogTemplate> {
        val json = context.assets.open("dialogs/dialogs.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json).getJSONObject("dialogs")
        val result = HashMap<Int, DialogTemplate>()
        for (key in root.keys()) {
            val d = root.getJSONObject(key)
            val controls = ArrayList<ControlTemplate>()
            val arr = d.getJSONArray("controls")
            for (i in 0 until arr.length()) {
                val c = arr.getJSONObject(i)
                val styles = HashSet<String>()
                val sa = c.getJSONArray("styles")
                for (j in 0 until sa.length()) styles.add(sa.getString(j))
                controls.add(
                    ControlTemplate(
                        text = c.getString("text"),
                        id = c.getInt("id"),
                        cls = c.getString("cls"),
                        styles = styles,
                        styleNum = c.optInt("styleNum", 0),
                        x = c.getInt("x"), y = c.getInt("y"), w = c.getInt("w"), h = c.getInt("h"),
                    )
                )
            }
            val styles = HashSet<String>()
            val sa = d.getJSONArray("styles")
            for (j in 0 until sa.length()) styles.add(sa.getString(j))
            val id = d.getInt("id")
            result[id] = DialogTemplate(
                id = id,
                w = d.getInt("w"), h = d.getInt("h"),
                caption = d.optString("caption", ""),
                styles = styles,
                menu = if (d.isNull("menu")) null else d.optString("menu"),
                controls = controls,
            )
        }
        return result
    }
}

/** Voce di menu (dal file menus/ZARROSIM.json). */
data class MenuItem(val kind: String, val label: String, val menuId: Int, val items: List<MenuItem>)

object Menus {
    fun load(context: Context, name: String): List<MenuItem> {
        val json = context.assets.open("menus/$name.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        fun parse(o: JSONObject): MenuItem {
            val items = ArrayList<MenuItem>()
            val sub = o.optJSONArray("items")
            if (sub != null) for (i in 0 until sub.length()) items.add(parse(sub.getJSONObject(i)))
            return MenuItem(o.optString("kind", "menuitem"), o.optString("label", ""), o.optInt("menu_id", -1), items)
        }
        val result = ArrayList<MenuItem>()
        for (i in 0 until arr.length()) result.add(parse(arr.getJSONObject(i)))
        return result
    }
}
