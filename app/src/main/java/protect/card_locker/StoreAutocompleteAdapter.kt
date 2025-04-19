package protect.card_locker

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView

data class Store(
    val name: String,
    val logo: ByteArray?,
) {
    override fun toString(): String = name

    val logoBitmap: Bitmap? by lazy {
        if (logo == null) return@lazy null
        val bitmap = BitmapFactory.decodeByteArray(logo, 0, logo.size)

        val borderSize = (bitmap.width * 0.1f).toInt()
        val newWidth = bitmap.width + borderSize * 2
        val newHeight = bitmap.height

        val output = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawARGB(0, 0, 0, 0) // sfondo trasparente

        canvas.drawBitmap(bitmap, borderSize.toFloat(), 0f, null)

        return@lazy output
    }

    companion object {
        @JvmStatic
        fun toStore(cursor: Cursor): Store {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val logo = cursor.getBlob(cursor.getColumnIndexOrThrow("logo"))

            return Store(name, logo)
        }
    }
}

class StoreAutocompleteAdapter(
    c: Context,
    val items: MutableList<Store>,
) : ArrayAdapter<Store>(c, 0, items), Filterable {

    private val filteredItems: MutableList<Store> = items.toMutableList()
    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            val query = constraint.toString().lowercase()
            val suggestions = mutableListOf<Store>()

            if (constraint.isNullOrEmpty()) {
                suggestions += items
            } else {
                val filteredList = items.filter { store ->
                    store.name.lowercase().startsWith(query)
                }
                suggestions += filteredList
            }

            filterResults.values = suggestions
            filterResults.count = suggestions.size
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredItems.clear()
            if (results?.values is List<*>) {
                @Suppress("UNCHECKED_CAST")
                filteredItems.addAll(results.values as List<Store>)
            }
            notifyDataSetChanged()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.store_dropdown_item, parent, false)

        val logoIv = view.findViewById<ImageView>(R.id.store_logo)
        val logoTv = view.findViewById<TextView>(R.id.store_name)

        logoTv.text = item.name
        item.logoBitmap?.let { logoIv.setImageBitmap(it) }

        return view
    }

    override fun getCount(): Int = filteredItems.size
    override fun getItem(position: Int): Store = filteredItems[position]
    override fun getFilter(): Filter = filter

}
