package model.dao;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UsuarioDao extends Remote{

	//Testando funcionalidades
	public abstract boolean teste() throws RemoteException;
	
	//usuario
	public abstract String criarUsuario(String name, String senha, String email) throws RemoteException;
	public abstract boolean login(String email, String senha) throws RemoteException;
	public abstract int idLogin(String email) throws RemoteException;
	
	//contatos
	public abstract String adicionarContatos(int idLogin, String emailAdicionado) throws RemoteException;
	public abstract String consultarContatos(int idLogin) throws RemoteException;
	public abstract String consultarContatosCliente(int idLogin) throws RemoteException;
	public abstract String apagarContatos(int idLogin, String apagarContato) throws RemoteException;
	
	
	//consultas	
	public abstract String findNameByEmail(int idLogin) throws RemoteException;
	public abstract String findById(int idLogin) throws RemoteException;
	public abstract int findIdByEmail(int idLogin) throws RemoteException;
	
	//mensagens
	public abstract String criarMensagens(int idLogin, String para, String assunto, String mensagens) throws RemoteException;
	public abstract String consultarMinhasMensagens(int idLogin) throws RemoteException;
	public abstract String consultarMinhasMensagensEnviadas(int idLogin) throws RemoteException;

	
	
	
	
}
