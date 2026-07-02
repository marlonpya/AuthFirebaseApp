package com.microsol.authfirebaseapp.presentation.cursos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microsol.authfirebaseapp.databinding.ItemCursoBinding
import com.microsol.authfirebaseapp.domain.model.Curso

/**
 * Adapter de la lista de cursos. [onClick] navega a Tareas (tocar la tarjeta), [onEditar] y
 * [onEliminar] se disparan desde los iconos de lápiz/basurero de cada fila.
 */
class CursosAdapter(
    private val onClick: (Curso) -> Unit,
    private val onEditar: (Curso) -> Unit,
    private val onEliminar: (Curso) -> Unit
) : ListAdapter<Curso, CursosAdapter.CursoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val binding = ItemCursoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CursoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CursoViewHolder(private val binding: ItemCursoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(curso: Curso) {
            binding.textNombreCurso.text = curso.nombre
            binding.textDescripcionCurso.text = curso.descripcion
            binding.root.setOnClickListener { onClick(curso) }
            binding.botonEditarCurso.setOnClickListener { onEditar(curso) }
            binding.botonEliminarCurso.setOnClickListener { onEliminar(curso) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Curso>() {
            override fun areItemsTheSame(oldItem: Curso, newItem: Curso) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Curso, newItem: Curso) = oldItem == newItem
        }
    }
}
