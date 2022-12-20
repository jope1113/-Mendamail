package model.dao.impl;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import db.DB;
import db.DbException;
import model.dao.UsuarioDao;

public class UsuarioDaoJDBC implements UsuarioDao {

	private ArrayList<Object> login = new ArrayList<>();
	private Connection conn;

	public UsuarioDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	public int idLogin(String email) {

		if (!email.contains("@mendamail.com")) {
			email += "@mendamail.com";
		}

		Random random = new Random();
		int numAleatorio = random.nextInt(1, 100000);

		login.add(numAleatorio);
		login.add(email);

		System.out.println(login);
		return numAleatorio;
	}

	public String confirmacaoUsuario(int idLogin) {

		int pos = login.indexOf(idLogin);
		int posEmail = pos + 1;
		String email = (String) login.get(posEmail);

		return email;
	}

	@Override
	public String criarUsuario(String name, String senha, String email) throws RemoteException {

		if (!email.contains("@mendamail.com")) {
			email += "@mendamail.com";
		}
		if (!isValidEmailAddressRegex(email) || isEmailUsed(email)) {
			return "E-mail digitado nao e valido ou ja existe";
		} else if (!isValidSenhaRegex(senha)) {
			return "A senha deve conter de 8-20 caracteres, com pelo menos um digito, um alfabeto maiúsculo,"
					+ "\num alfabeto minúsculo, caractere especial e nao possuir espacos em branco.\n";
		} else {
			email = email.toLowerCase();
			int id = nextId();
			PreparedStatement st = null;

			String str = name.substring(0, 1);
			String strResto = name.substring(1, name.length());
			str = str.toUpperCase();
			name = str + strResto;

			try {
				st = conn.prepareStatement("INSERT INTO Usuario (Email, Senha, ID, Nome) " + "VALUES (?, ?, ?, ?)");
				st.setString(1, email);
				st.setString(2, senha);
				st.setInt(3, id);
				st.setString(4, name);
				st.executeUpdate();

				return "Usuario registrado com sucesso";

			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			} finally {
				DB.closeStatament(st);
			}
		}
	}

	@Override
	public boolean login(String email, String senha) throws RemoteException {

		String senhaBanco;
		email = email.toLowerCase();

		if (!email.contains("@mendamail.com")) {
			email += "@mendamail.com";
		}
		if (isEmailUsed(email)) {
			PreparedStatement st = null;
			ResultSet rs = null;
			try {
				st = conn.prepareStatement("SELECT Senha " + "FROM Usuario " + "WHERE Email = ?");

				st.setString(1, email);
				rs = st.executeQuery();
				rs.next();
				senhaBanco = (String) rs.getString("Senha");

			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			} finally {
				DB.closeResultSet(rs);
				DB.closeStatament(st);
			}
			if (senha.equals(senhaBanco)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public String findNameByEmail(int idLogin) throws RemoteException {

		String email = confirmacaoUsuario(idLogin);

		if (!email.contains("@mendamail.com")) {
			email += "@mendamail.com";
		}

		String nome = "";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT Nome " + "FROM Usuario " + "WHERE Email = ?");

			st.setString(1, email);
			rs = st.executeQuery();
			if (rs.next()) {
				nome = rs.getString("Nome");

				return nome;
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatament(st);
		}
		return nome;
	}

	@Override
	public int findIdByEmail(int idLogin) throws RemoteException {
		String email = confirmacaoUsuario(idLogin);

		int id = 0;

		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT ID " + "FROM Usuario " + "WHERE Email = ?");

			st.setString(1, email);
			rs = st.executeQuery();
			if (rs.next()) {
				id = rs.getInt("ID");
				return id;
			}

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatament(st);
		}
		return id;
	}

	@Override
	public String findById(int id) throws RemoteException {
		String email = null;
		PreparedStatement st = null;
		ResultSet rs = null;

		try {

			st = conn.prepareStatement("SELECT Email " + "FROM Usuario " + "WHERE ID = ?");
			st.setInt(1, id);
			rs = st.executeQuery();
			if (rs.next()) {
				email = rs.getString("Email");
				return email;
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatament(st);
		}
		return email;
	}

	public int nextId() throws RemoteException {
		int id = 0;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement("SELECT MAX(id) as maxId FROM Usuario");
			rs = st.executeQuery();
			rs.next();
			id = rs.getInt("maxId");

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatament(st);
		}
		return id + 1;
	}

	// Verificar se o e-mail e valido
	public boolean isValidEmailAddressRegex(String email) throws RemoteException {
		boolean isEmailIdValid = false;

		if (email != null && email.length() > 0) {
			String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
			Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(email);
			if (matcher.matches()) {
				isEmailIdValid = true;
			}
		}
		return isEmailIdValid;
	}

	public boolean isValidSenhaRegex(String senha) throws RemoteException {
		boolean isSenhaValid = false;

		if (senha != null && senha.length() > 0) {
			String expression = "^(?=.*[0-9])" + "(?=.*[a-z])(?=.*[A-Z])" + "(?=.*[@#$%^&+=])" + "(?=\\S+$).{8,20}$";
			Pattern pattern = Pattern.compile(expression);
			Matcher matcher = pattern.matcher(senha);
			if (matcher.matches()) {
				isSenhaValid = true;
			}
		}
		return isSenhaValid;
	}

	@Override
	public String apagarContatos(int idLogin, String apagarContato) throws RemoteException {

		String lista = consultarContatos(idLogin);
		String[] listaSeparada = lista.split(",");
		String listaAtualizada = "";

		for (int i = 0; i < listaSeparada.length; i++) {
			if (consultarContatos(idLogin) != null && listaSeparada[i].equals(apagarContato)) {

			} else if (consultarContatos(idLogin) != "" && listaSeparada.length == 1
					&& listaSeparada[i].equals(apagarContato) && i == 0) {
				listaAtualizada = null;
			} else if (i == listaSeparada.length - 1 || listaSeparada.length == 1 && i == 0) {
				listaAtualizada += listaSeparada[i];
			} else if (i <= listaSeparada.length - 2) {
				listaAtualizada += listaSeparada[i] + ",";
			}
		}
		updateContatos(idLogin, listaAtualizada);
		return "Contato apagado com sucesso";
	}

	@Override
	public String adicionarContatos(int idLogin, String emailAdicionado) throws RemoteException {

		String email = confirmacaoUsuario(idLogin);

		String contatos = null;
		String resposta = null;

		if (isValidEmailAddressRegex(emailAdicionado) && isEmailUsed(emailAdicionado)) {

			if (consultarContatos(idLogin) == null || consultarContatosCliente(idLogin) == "Nao ha contatos") {
				contatos = emailAdicionado.toLowerCase();
				resposta = "Contato adicionado";

			} else if (consultarContatos(idLogin).charAt(consultarContatos(idLogin).length() - 1) == ','
					&& !consultarContatos(idLogin).contains(emailAdicionado)) {
				contatos = consultarContatos(idLogin) + emailAdicionado.toLowerCase();
				resposta = "Contato adicionado";
			} else if (!consultarContatos(idLogin).contains(emailAdicionado)) {
				contatos = consultarContatos(idLogin) + "," + emailAdicionado.toLowerCase();
				resposta = "Contato adicionado";
			} else {
				contatos = consultarContatos(idLogin);
				resposta = "Este contato ja esta na sua lista de contatos";
			}
			PreparedStatement st = null;
			ResultSet rs = null;

			try {
				st = conn.prepareStatement("update usuario set contatos = ? where email = ?");

				st.setString(2, email);
				st.setString(1, contatos);
				st.executeUpdate();
				return resposta;
			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			} finally {
				DB.closeResultSet(rs);
				DB.closeStatament(st);
			}
		} else {
			resposta = "O e-mail que você tentou adicionar e invalido.";
			return resposta;
		}
	}

	@Override
	public String consultarContatosCliente(int idLogin) throws RemoteException {
		String email = confirmacaoUsuario(idLogin);
		String lista;
		String resposta = "Contatos:\n";
		String[] listaSeparada;
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement("SELECT Contatos " + "FROM Usuario " + "WHERE email = ?");

			st.setString(1, email);
			rs = st.executeQuery();
			rs.next();

			lista = rs.getString("Contatos");

			if (lista == null) {
				lista = "Nao ha contatos";

			}
			if (!lista.equals("") && !lista.equals("Nao ha contatos")) {

				listaSeparada = lista.split(",");

				for (int i = 0; i < listaSeparada.length; i++) {
					if (listaSeparada[i] == "" || listaSeparada[i] == " ") {
					} else {
						resposta += i + 1 + "# " + listaSeparada[i] + " \n";
					}
				}
				return resposta;
			} else {
				lista = "Nao ha contatos";
				return lista;
			}

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatament(st);
		}
	}

	public String consultarContatos(int idLogin) {

		String email = confirmacaoUsuario(idLogin);

		String lista = "";
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement("SELECT Contatos " + "FROM Usuario " + "WHERE email = ?");
			st.setString(1, email);
			rs = st.executeQuery();
			rs.next();
			lista = rs.getString("Contatos");

			return lista;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatament(st);
		}
	}

	public void updateContatos(int idLogin, String alteracao) throws RemoteException {

		PreparedStatement st = null;
		int id = findIdByEmail(idLogin);

		try {
			st = conn.prepareStatement("update usuario set contatos = ? where id = ?");
			st.setString(1, alteracao);
			st.setInt(2, id);
			st.executeUpdate();

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatament(st);
		}
	}

	public boolean isEmailUsed(String email) throws RemoteException {
		int count = 0;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement("SELECT email FROM Usuario");
			rs = st.executeQuery();

			while (rs.next()) {
				if (email.equals(rs.getString("Email"))) {
					count++;
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatament(st);
		}
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean teste() throws RemoteException {

		criarUsuario("Teste", "T&ste123", "teste123@mendamail.com");
		login("teste123@mendamail.com", "T&ste123");
		int idLogin = idLogin("teste123@mendamail.com");
		adicionarContatos(idLogin, "admin@email.com");
		adicionarContatos(idLogin, "admin2@email.com");
		adicionarContatos(idLogin, "admin3@email.com");
		apagarContatos(idLogin, "admin2@email.com");

		if (!login("teste123@mendamail.com", "T&ste123")) {
			System.out.println("Falha no teste de login");
			return false;
		} else if (!consultarContatos(idLogin).equals("admin@email.com,admin3@email.com")) {
			System.out.println("Falha no teste dos contatos");
			return false;
		} else {
			PreparedStatement st = null;

			try {

				st = conn.prepareStatement("delete from usuario where id = ?");
				st.setInt(1, findIdByEmail(idLogin));
				st.executeUpdate();

			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			} finally {
			}
			DB.closeStatament(st);
		}

		return true;
	}

	@Override
	public String criarMensagens(int idLogin, String para, String assunto, String mensagens) throws RemoteException {

		String email = confirmacaoUsuario(idLogin);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date data = new Date();
		email = email.toLowerCase();
		para = para.toLowerCase();

		if (isEmailUsed(para) && isValidEmailAddressRegex(para)) {

			PreparedStatement st = null;
			ResultSet rs = null;

			try {
				st = conn.prepareStatement(
						"INSERT INTO Mensagens (De, Para, Assunto, Mensagem, Dia_hora) " + "VALUES (?, ?, ?, ?, ?)");
				st.setString(1, email);
				st.setString(2, para);
				st.setString(3, assunto);
				st.setString(4, mensagens);
				st.setString(5, sdf.format(data));
				st.executeUpdate();

				return "E-mail enviado com sucesso.";
			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			} finally {
				DB.closeResultSet(rs);
				DB.closeStatament(st);
			}
		} else {

			return "E-mail digitado invalido";
		}
	}

	@Override
	public String consultarMinhasMensagens(int idLogin) throws RemoteException {

		String email = confirmacaoUsuario(idLogin);
		String emails = "";
		String[] mensagemSeparada;
		String mensagemAjustada = "";

		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement("SELECT * " + "FROM Mensagens " + "WHERE Para = ?");
			st.setString(1, email);
			rs = st.executeQuery();
			while (rs.next()) {
				mensagemSeparada = rs.getString("mensagem").split(" ");
				for (int i = 0; i < mensagemSeparada.length; i++) {
					mensagemAjustada += mensagemSeparada[i] + " ";
					if (i % 10 == 0 && i > 1) {
						mensagemAjustada += " \n";
					}
				}
				emails += "De: " + rs.getString("De") + "\nPara: " + rs.getString("para") + "\nData: "
						+ rs.getString("Dia_hora").substring(0, 10) + ", as "
						+ rs.getString("Dia_hora").substring(11, 19) + "\nAssunto: " + rs.getString("Assunto")
						+ "\nMensagem: " + mensagemAjustada + "\n\n\n";

				mensagemAjustada = "";
			}

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatament(st);
		}
		return emails;
	}

	public String consultarMinhasMensagensEnviadas(int idLogin) throws RemoteException {
		String email = confirmacaoUsuario(idLogin);
		String emails = "";
		String[] mensagemSeparada;
		String mensagemAjustada = "";
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement("SELECT * " + "FROM Mensagens " + "WHERE De = ?");

			st.setString(1, email);
			rs = st.executeQuery();
			while (rs.next()) {
				mensagemSeparada = rs.getString("mensagem").split(" ");
				for (int i = 0; i < mensagemSeparada.length; i++) {
					mensagemAjustada += mensagemSeparada[i] + " ";
					if (i % 10 == 0 && i > 1) {
						mensagemAjustada += " \n";
					}
				}
				emails += "De: " + rs.getString("De") + "\nPara: " + rs.getString("para") + "\nData: "
						+ rs.getString("Dia_hora").substring(0, 10) + ", as "
						+ rs.getString("Dia_hora").substring(11, 19) + "\nAssunto: " + rs.getString("Assunto")
						+ "\nMensagem: " + mensagemAjustada + "\n\n\n";
				mensagemAjustada = "";
			}

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatament(st);
		}
		return emails;
	}
}