package application;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import model.dao.DaoFactory;
import model.dao.UsuarioDao;

public class Servidor {

	public static void main(String[] args) throws RemoteException {

		// Conexao
		Registry registro = null;
		UsuarioDao usuario = DaoFactory.createUsuarioDao();

		try {
			Remote remote = UnicastRemoteObject.exportObject(usuario, 0);
			registro = LocateRegistry.createRegistry(3333);
			registro.rebind("Email", remote);

		} catch (Exception e) {
			System.out.println("Erro no Servidor:" + e.getMessage());
		}
		if (usuario.teste()) {
			System.out.println("Funcoes testadas");
		}
		System.out.println("Servidor online");
	}
}