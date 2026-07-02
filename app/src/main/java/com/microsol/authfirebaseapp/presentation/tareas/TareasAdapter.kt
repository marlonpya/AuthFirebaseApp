package com.microsol.authfirebaseapp.presentation.tareas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microsol.authfirebaseapp.R
import com.microsol.authfirebaseapp.databinding.ItemTareaBinding
import com.microsol.authfirebaseapp.domain.model.Tarea
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter de la lista de tareas. [onToggle] se llama al tocar el checkbox de "completada",
 * [onEditar] y [onEliminar] desde los iconos de lápiz/basurero de cada fila.
 */
class TareasAdapter(
    private val onToggle: (Tarea) -> Unit,
    private val onEditar: (Tarea) -> Unit,
    private val onEliminar: (Tarea) -> Unit
) : ListAdapter<Tarea, TareasAdapter.TareaViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val binding = ItemTareaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TareaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TareaViewHolder(private val binding: ItemTareaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(tarea: Tarea) {
            binding.checkCompletada.text = tarea.titulo
            binding.checkCompletada.isChecked = tarea.completada
            binding.textFechaLimite.text = binding.root.context.getString(
                R.string.tareas_fecha_limite,
                formatoFecha.format(Date(tarea.fechaLimite))
            )
            binding.checkCompletada.setOnClickListener { onToggle(tarea) }
            binding.botonEditarTarea.setOnClickListener { onEditar(tarea) }
            binding.botonEliminarTarea.setOnClickListener { onEliminar(tarea) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Tarea>() {
            override fun areItemsTheSame(oldItem: Tarea, newItem: Tarea) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Tarea, newItem: Tarea) = oldItem == newItem
        }
    }
}
