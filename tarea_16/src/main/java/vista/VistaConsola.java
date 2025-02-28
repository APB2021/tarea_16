package vista;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.function.Consumer;

import dao.IAlumnosDao;
import dao.AlumnosBD;
import modelo.Alumno;
import modelo.Grupo;
import pool.PoolConexiones;

/**
 * Esta clase se encarga de gestionar el menú principal de la aplicación.
 * Contiene las opciones para interactuar con los grupos y los alumnos.
 */

public class VistaConsola implements IVista {

	private final Scanner sc = new Scanner(System.in); // Scanner para la entrada de datos
	private final IAlumnosDao alumnosDao = new AlumnosBD(); // Gestor para alumnos y grupos

	/**
	 * Muestra el menú principal y permite seleccionar una opción. Permite al
	 * usuario seleccionar opciones como insertar alumnos, grupos, mostrar todos los
	 * alumnos y guardar los datos de los alumnos en un archivo de texto.
	 */

	/**
	 * Clase encargada de mostrar el menú principal y gestionar las interacciones
	 * con el usuario para operar con alumnos y grupos.
	 */
	public void mostrarMenu(IAlumnosDao modelo) {
		int opcion;
		do {
			imprimirMenu();
			System.out.print("Selecciona una opción: ");
			try {
				opcion = sc.nextInt();
				sc.nextLine(); // Limpiar buffer
				gestionarOpcion(opcion, modelo);
			} catch (InputMismatchException e) {
				System.out.println("Entrada no válida. Por favor, introduce un número.");
				sc.nextLine(); // Limpiar buffer en caso de error
				opcion = -1; // Reiniciar opción para evitar salir del bucle
			}
		} while (opcion != 0);
	}

	/**
	 * Imprime las opciones disponibles en el menú principal.
	 */
	private void imprimirMenu() {

		String textoMenu = """
				---- Menú Principal -------------------------------------------
				1. Insertar nuevo alumno.
				2. Insertar nuevo grupo.
				3. Mostrar todos los alumnos.
				4. Guardar todos los alumnos en un fichero de texto.
				5. Leer alumnos de un fichero de texto y guardarlos en la BD.
				6. Modificar el nombre de un alumno por su NIA.
				7. Eliminar un alumno a partir de su NIA.
				8. Eliminar los alumnos del grupo indicado.
				9. Guardar grupos y alumnos en un archivo XML.
				10. Leer un archivo XML de grupos y guardar los datos en la BD.
				---- Menú Tarea 16 --------------------------------------------
				11. Mostrar todos los alumnos del grupo elegido.
				12. Mostrar todos los datos de un alumno por su NIA.
				13. Cambiar de grupo al alumno que elija el usuario.
				14. Guardar el grupo que elija el usuario en un fichero XML.
				0. Salir.
				---------------------------------------------------------------
				""";

		System.out.println(textoMenu);
	}

	/**
	 * Gestiona la opción seleccionada por el usuario y llama al método
	 * correspondiente.
	 * 
	 * @param opcion Opción seleccionada por el usuario.
	 * @param modelo Modelo de datos de alumnos y grupos.
	 */
	private void gestionarOpcion(int opcion, IAlumnosDao modelo) {
		switch (opcion) {

		case 1 -> insertarNuevoAlumno(modelo);
		case 2 -> insertarNuevoGrupo(modelo);
		case 3 -> mostrarTodosLosAlumnos(true); // Mostrará toda la información de todos los alumnos
		case 4 -> guardarAlumnosEnFicheroTexto();
		case 5 -> leerAlumnosDesdeFichero();
		// case 6 -> modificarNombreAlumnoPorNia();
		case 6 -> ejecutarOperacionConNIA(null, null, null);
		case 7 -> eliminarAlumnoPorNIA();
		case 8 -> eliminarAlumnosPorGrupo();
		case 9 -> guardarGruposEnXML();
		case 10 -> leerYGuardarGruposXML();
		// Tarea 16:
		case 11 -> mostrarAlumnosPorGrupo(modelo);
		case 12 -> mostrarTodosLosAlumnos(false); // Mostrará únicamente el nia y el nombre de todos los alumnos
		case 13 -> cambiarGrupoAlumno(modelo);
		case 14 -> guardarGrupoEspecificoEnXML(modelo);
		case 0 -> System.out.println("Saliendo del programa...");
		default -> System.out.println("Opción no válida. Intenta de nuevo.");
		}
	}

	/**
	 * Permite insertar un nuevo alumno solicitando los datos al usuario y
	 * almacenándolos en la base de datos.
	 * 
	 * @param modelo el objeto DAO para gestionar las operaciones de alumnos.
	 */
	public void insertarNuevoAlumno(IAlumnosDao modelo) {
		try {
			// Solicitar datos del alumno al usuario
			Alumno alumno = modelo.solicitarDatosAlumno();

			// Uso de try-with-resources para manejar automáticamente la conexión
			try (Connection conexionBD = PoolConexiones.getConnection()) {
				// Insertar el alumno en la base de datos
				if (alumnosDao.insertarAlumno(conexionBD, alumno)) {
					System.out.println("Alumno insertado correctamente.");
				} else {
					System.out.println("Error al insertar el alumno.");
				}
			}
		} catch (Exception e) {
			System.out.println("Ocurrió un error al insertar el alumno: " + e.getMessage());
			e.printStackTrace(); // Para registrar el stack trace en caso de errores inesperados
		}
	}

	/**
	 * Permite insertar un nuevo grupo solicitando los datos al usuario y
	 * almacenándolos en la base de datos.
	 *
	 * @param modelo el DAO que permite interactuar con la base de datos.
	 */
	public void insertarNuevoGrupo(IAlumnosDao modelo) {
		String nombreGrupo;

		// Solicitar al usuario el nombre del nuevo grupo con validación
		while (true) {
			System.out.println("Introduce el nombre del nuevo grupo (una letra):");
			nombreGrupo = sc.nextLine().toUpperCase().trim();

			// Validamos que el nombre sea solo una letra
			if (nombreGrupo.length() == 1 && nombreGrupo.matches("[A-Za-z]")) {
				break; // Salimos del bucle si la validación es exitosa
			} else {
				System.out.println("El nombre del grupo debe ser una sola letra.");
			}
		}

		// Crear objeto Grupo
		Grupo grupo = new Grupo(nombreGrupo);

		// Intentamos insertar el nuevo grupo utilizando try-with-resources
		try (Connection conexionBD = PoolConexiones.getConnection()) {
			// Llamamos al método del DAO para insertar el grupo
			if (modelo.insertarGrupo(conexionBD, grupo)) {
				System.out.println("Grupo insertado correctamente.");
			} else {
				System.out.println("Error al insertar el grupo.");
			}
		} catch (Exception e) {
			System.out.println("Ocurrió un error al insertar el grupo: " + e.getMessage());
		}
	}

	public void mostrarTodosLosAlumnos(boolean mostrarTodaLaInformación) {
		try (Connection conexionBD = PoolConexiones.getConnection()) {
			if (alumnosDao.mostrarTodosLosAlumnos(conexionBD, mostrarTodaLaInformación)) {
				System.out.println("Los alumnos se han mostrado correctamente.");
			} else {
				System.out.println("No se pudieron mostrar los alumnos.");
			}
		} catch (Exception e) {
			System.out.println("Ocurrió un error al mostrar los alumnos: " + e.getMessage());
		}
	}

	/**
	 * Permite guardar todos los alumnos en un archivo de texto. Recupera la
	 * información de los alumnos de la base de datos y la guarda en un archivo
	 * llamado "alumnos.txt". La información incluye: nombre, apellidos, género,
	 * fecha de nacimiento, ciclo, curso y nombre del grupo.
	 */
	public void guardarAlumnosEnFicheroTexto() {
		try {
			// Recuperamos todos los alumnos
			Connection conexionBD = PoolConexiones.getConnection();

			// Llamamos al método que guarda los alumnos en el fichero de texto sin esperar
			// un valor booleano
			alumnosDao.guardarAlumnosEnFicheroTexto(conexionBD);

			System.out.println("Alumnos guardados correctamente en el archivo de texto.");

		} catch (Exception e) {
			System.out.println("Ocurrió un error al guardar los alumnos en el archivo de texto: " + e.getMessage());
		}
	}

	/**
	 * Permite leer alumnos desde el fichero fijo "alumnos.txt" y guardarlos en la
	 * base de datos.
	 */
	public void leerAlumnosDesdeFichero() {
		try {
			Connection conexionBD = PoolConexiones.getConnection();

			if (alumnosDao.leerAlumnosDeFicheroTexto(conexionBD)) {
				System.out.println("Alumnos leídos e insertados correctamente desde el fichero 'alumnos.txt'.");
			} else {
				System.out.println("Ocurrió un error al procesar el fichero.");
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	// TAREA 16:

	public boolean ejecutarOperacionConNIA(Connection conexionBD, String sql,
			Consumer<PreparedStatement> configuracionParams) {
		return false;

	};

	/**
	 * Permite modificar el nombre de un alumno solicitando su NIA y el nuevo
	 * nombre.
	 */
	public void modificarNombreAlumnoPorNia() {
		try {
			// Solicitar al usuario el NIA del alumno
			System.out.print("Introduce el NIA del alumno cuyo nombre quieres modificar: ");
			int nia = sc.nextInt();
			sc.nextLine(); // Limpiar buffer

			// Solicitar el nuevo nombre del alumno
			System.out.print("Introduce el nuevo nombre para el alumno: ");
			String nuevoNombre = sc.nextLine().trim().toUpperCase();

			// Validar que el nombre no esté vacío
			if (nuevoNombre.isEmpty()) {
				System.out.println("El nombre no puede estar vacío.");
				return;
			}

			// Usar try-with-resources para gestionar la conexión a la base de datos
			try (Connection conexionBD = PoolConexiones.getConnection()) {
				// Llamar al método del gestor para modificar el nombre
				if (alumnosDao.modificarNombreAlumnoPorNIA(conexionBD, nia, nuevoNombre)) {
					System.out.println("Nombre del alumno modificado correctamente.");
				} else {
					System.out.println("No se pudo modificar el nombre del alumno. Verifica el NIA.");
				}
			} // La conexión se cierra automáticamente aquí
		} catch (Exception e) {
			System.out.println("Ocurrió un error al modificar el nombre del alumno: " + e.getMessage());
		}
	}

	/**
	 * Permite eliminar un alumno de la base de datos a partir de su NIA (PK).
	 */
	public void eliminarAlumnoPorNIA() {
		try {
			// Solicitar NIA al usuario
			System.out.println("Introduce el NIA del alumno a eliminar:");
			int nia = sc.nextInt();
			sc.nextLine(); // Limpiar buffer

			// Usar try-with-resources para manejar la conexión a la base de datos
			try (Connection conexionBD = PoolConexiones.getConnection()) {
				// Intentar eliminar el alumno
				if (alumnosDao.eliminarAlumnoPorNIA(conexionBD, nia)) {
					System.out.println("Alumno eliminado correctamente.");
				} else {
					System.out.println("No se encontró un alumno con el NIA proporcionado.");
				}
			}
		} catch (Exception e) {
			System.out.println("Ocurrió un error al intentar eliminar el alumno: " + e.getMessage());
			// Aquí también podrías añadir un log en nivel ERROR si estás usando Log4j
		}
	}

	/**
	 * Elimina los alumnos del grupo indicado por el usuario. Muestra previamente
	 * los grupos existentes y permite al usuario seleccionar uno. Luego elimina a
	 * todos los alumnos que pertenezcan al grupo seleccionado.
	 */
	public void eliminarAlumnosPorGrupo() {
		try (Connection conexionBD = PoolConexiones.getConnection()) {
			// Mostramos los grupos disponibles
			System.out.println("Grupos disponibles:");
			if (!AlumnosBD.mostrarTodosLosGrupos(conexionBD)) {
				System.out.println("No hay grupos registrados.");
				return;
			}

			// Pedimos al usuario el nombre del grupo a eliminar
			System.out.println("Introduce el nombre del grupo cuyos alumnos deseas eliminar:");
			String nombreGrupo = sc.nextLine().toUpperCase().trim();

			// Confirmamos la operación con el usuario
			System.out.println(
					"¿Estás seguro de que deseas eliminar todos los alumnos del grupo " + nombreGrupo + "? (S/N)");
			String confirmacion = sc.nextLine().toUpperCase().trim();

			if (!confirmacion.equals("S")) {
				System.out.println("Operación cancelada por el usuario.");
				return;
			}

			// Llamamos al gestor para realizar la operación
			if (alumnosDao.eliminarAlumnosPorGrupo(conexionBD, nombreGrupo)) {
				System.out.println("Alumnos del grupo " + nombreGrupo + " eliminados correctamente.");
			} else {
				System.out.println(
						"No se pudieron eliminar los alumnos del grupo especificado. Verifica el nombre del grupo.");
			}
		} catch (Exception e) {
			System.out.println("Ocurrió un error al eliminar alumnos por grupo: " + e.getMessage());
		}
	}

	/**
	 * Método que se encarga de guardar los grupos y sus alumnos en un archivo XML.
	 * Utiliza la conexión a la base de datos proporcionada por el pool de
	 * conexiones.
	 */
	public void guardarGruposEnXML() {
		// Usamos try-with-resources para gestionar automáticamente el cierre de la
		// conexión
		try (Connection conexionBD = PoolConexiones.getConnection()) {
			// Llamamos al método de GestorGrupos para guardar los grupos en el archivo XML
			if (AlumnosBD.guardarGruposEnXML(conexionBD)) {
				// Si el proceso es exitoso, mostramos un mensaje de éxito
				System.out.println("Archivo XML guardado correctamente.");
			} else {
				// Si hubo algún error, mostramos un mensaje de fallo
				System.out.println("Error al guardar el archivo XML.");
			}
		} catch (Exception e) {
			// Capturamos cualquier excepción y mostramos el mensaje de error
			System.out.println("Ocurrió un error al guardar los grupos en XML: " + e.getMessage());
		}
	}

	/**
	 * Lee el archivo XML de grupos (alumnos.xml) y guarda los datos en la base de
	 * datos MySQL. Si ocurre un error durante el proceso, se captura la excepción y
	 * se muestra un mensaje de error.
	 */
	public void leerYGuardarGruposXML() {
		// Ruta fija del archivo XML de grupos
		String rutaArchivo = "grupos.xml";

		// Verificamos si el archivo existe
		File archivoXML = new File(rutaArchivo);
		if (!archivoXML.exists()) {
			System.out.println("El archivo XML no existe en la ruta especificada: " + rutaArchivo);
			return; // Salimos del método si el archivo no existe
		}

		// Usamos try-with-resources para manejar la conexión
		try (Connection conexionBD = PoolConexiones.getConnection()) {
			// Llamamos al método de GestorGrupos para leer los grupos en el archivo XML
			if (AlumnosBD.leerYGuardarGruposXML(rutaArchivo, conexionBD)) {
				// Si el proceso es exitoso, mostramos un mensaje de éxito
				System.out.println("Archivo XML leído correctamente y datos guardados en la base de datos.");
			} else {
				// Si hubo un error en el proceso, mostramos un mensaje de fallo
				System.out.println("Error al procesar el archivo XML.");
			}
		} catch (Exception e) {
			// Capturamos cualquier excepción y mostramos el mensaje de error
			System.out.println("Ocurrió un error al leer los grupos en XML: " + e.getMessage());
		}
	}

	// Tarea 16:

	/**
	 * Muestra los alumnos del grupo seleccionado por el usuario.
	 * 
	 * @param modelo Objeto que implementa la interfaz IAlumnosDao para realizar
	 *               operaciones con la base de datos.
	 */
	public void mostrarAlumnosPorGrupo(IAlumnosDao modelo) {
		try (Connection conexionBD = PoolConexiones.getConnection()) {
			// Llamar al método mostrarAlumnosPorGrupo desde el modelo (AlumnosBD)
			((AlumnosBD) modelo).mostrarAlumnosPorGrupo(conexionBD);
		} catch (Exception e) {
			System.out.println("Se produjo un error al intentar mostrar los alumnos por grupo. Revisa los logs.");
		}
	}

	/**
	 * Cambia de grupo al alumno seleccionado por el usuario.
	 * 
	 * @param modelo Objeto que implementa la interfaz IAlumnosDao para realizar
	 *               operaciones con la base de datos.
	 */
	public void cambiarGrupoAlumno(IAlumnosDao modelo) {
		try (Connection conexionBD = PoolConexiones.getConnection()) {
			// Llamar al método cambiarGrupoAlumno desde el modelo (AlumnosBD)
			((AlumnosBD) modelo).cambiarGrupoAlumno(conexionBD);
		} catch (Exception e) {
			System.out.println("Se produjo un error al intentar cambiar al alumno de grupo. Revisa los logs.");
		}

	}
	
	/**
	 * Guarda un grupo específico con toda su información (incluyendo los alumnos)
	 * en un archivo XML.
	 * 
	 * @param conexionBD  La conexión activa a la base de datos MySQL.
	 * @param numeroGrupo El número del grupo que se desea guardar.
	 * @return true si el archivo se guarda correctamente, false si ocurre un error.
	 */
	public void guardarGrupoEspecificoEnXML(IAlumnosDao modelo) {
		try (Connection conexionBD = PoolConexiones.getConnection()) {
			// Llamar al método guardarGrupoEspecificoEnXML desde el modelo (AlumnosBD)
			((AlumnosBD) modelo).guardarGrupoEspecificoEnXML(conexionBD);
		} catch (Exception e) {
			System.out.println("Se produjo un error al intentar cambiar al alumno de grupo. Revisa los logs.");
		}
	}

}