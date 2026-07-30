package com.microsol.authfirebaseapp.presentation.notificaciones

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microsol.authfirebaseapp.databinding.ItemNotificacionBinding
import com.microsol.authfirebaseapp.domain.model.Notificacion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter de la lista de notificaciones. [onClick] marca la notificación como leída (tocar la fila)
 * y [onEliminar] se dispara desde el icono de basurero. Las notificaciones sin leer se muestran en
 * negrita con un punto indicador; las leídas, en estilo normal y sin punto.
 */
class NotificacionesAdapter(
    private val onClick: (Notificacion) -> Unit,
    private val onEliminar: (Notificacion) -> Unit
) : ListAdapter<Notificacion, NotificacionesAdapter.NotificacionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val binding = ItemNotificacionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificacionViewHolder(private val binding: ItemNotificacionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notificacion: Notificacion) {
            binding.textTituloNotificacion.text = notificacion.titulo
            binding.textCuerpoNotificacion.text = notificacion.cuerpo
            binding.textFechaNotificacion.text = FORMATO_FECHA.format(Date(notificacion.fechaRecepcion))

            // Estilo según leída/no leída: negrita + punto azul cuando aún no se ha leído.
            val estilo = if (notificacion.leida) Typeface.NORMAL else Typeface.BOLD
            binding.textTituloNotificacion.setTypeface(null, estilo)
            binding.viewIndicadorNoLeida.visibility =
                if (notificacion.leida) View.INVISIBLE else View.VISIBLE

            binding.root.setOnClickListener { onClick(notificacion) }
            binding.botonEliminarNotificacion.setOnClickListener { onEliminar(notificacion) }
        }
    }

    companion object {
        // El patrón dd/MM/yyyy HH:mm es fijo (no localizado) para que la lección sea predecible.
        private val FORMATO_FECHA = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Notificacion>() {
            override fun areItemsTheSame(oldItem: Notificacion, newItem: Notificacion) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Notificacion, newItem: Notificacion) =
                oldItem == newItem
        }
    }
}
