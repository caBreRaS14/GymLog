package co.fexe.gymlog.data

class RegistroRepository(private val dao: RegistroDao) {
    val registros = dao.todos()
    suspend fun guardar(r: Registro) = if (r.id == 0L) dao.insertar(r) else dao.actualizar(r)
    suspend fun borrar(r: Registro) = dao.eliminar(r)
    suspend fun fechaDe(id: Long): Long? = if (id == 0L) null else dao.fechaDe(id)
    suspend fun reemplazarTodo(lista: List<Registro>) { dao.borrarTodo(); lista.forEach { dao.insertar(it) } }
}