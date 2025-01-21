package vista;

import dao.IAlumnosDao;

public interface IVista {

	public void mostrarMenu(IAlumnosDao modelo);

	// case 1:
	public void insertarNuevoAlumno(IAlumnosDao modelo);

	// case 2:
	public void insertarNuevoGrupo(IAlumnosDao modelo);

	// cases 3 y 12:
	public void mostrarTodosLosAlumnos(boolean mostrarTodaLaInformacion);

	// case 4:
	public void guardarAlumnosEnFicheroTexto();

	// case 5:
	public void leerAlumnosDesdeFichero();

	// MÉTODO GENÉRICO:

	// case 6:
	public void modificarNombreAlumnoPorNia();

	// case 7:
	public void eliminarAlumnoPorNIA();

	// case 8:
	public void eliminarAlumnosPorGrupo();

	// case 9:
	public void guardarGruposEnXML();

	// case 10:
	public void leerYGuardarGruposXML();

	// case 11:
	public void mostrarAlumnosPorGrupo(IAlumnosDao modelo);

}
