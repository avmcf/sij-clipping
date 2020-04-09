package com.vconsulte.sij.clipping;

//***************************************************************************************************
//	Clipping: Rotina de clipping das publicações do diario oficial 	
//
//
//	versao 2.0.0 	- 19 de Novembro de 2018
//					Versao Inicial
//
//	versao 2.0.1 	- 20 de Novembro de 2018
//					Inclusão de linha de comentario no arquivo tokens.txt
//
//	versao 2.1.0 	- 17 de Dezembro de 2018
//					Pemitir arquivos de tokens por TRT
//
//---------------------------------------------------------------------------------------------------------------------------
//	Nova versão da antiga classe clippingDO agora chamada de SplitDO
//
//	versao 2.0 		- .. de Junho de 2018
//					Classe renomeada para SplitDO.java
//					Integração CMIS
//					Gravação dos editais diretamente no servidor Alfresco
//
//	versao 2.1 		- 18 de Fevereiro de 2019
//					Novo query para selecao de editais conforme o novo modelo de dados
//	
//	versao 2.2 		- 24 de Fevereiro de 2019
//					Correção no query de indexação
//
//	versao 3 		- 04 de Março de 2020
//					Versão compativel com o Splitter_3.0
//
// 	V&C Consultoria Ltda.
//	Autor: Arlindo Viana.
//***************************************************************************************************

	import java.io.IOException;
	import java.text.SimpleDateFormat;
	import java.util.List;
	
	import javax.swing.*;
	import javax.swing.event.*;
	import java.util.*;
	import java.awt.*;
	import java.awt.event.*;
	
	import org.apache.chemistry.opencmis.client.api.Folder;
	import org.apache.chemistry.opencmis.client.api.Session;
	
	import com.vconsulte.sij.base.InterfaceServidor;   

public class Clipping extends JPanel implements ActionListener {


	private static final long serialVersionUID = 1L;

	static InterfaceServidor conexao = new InterfaceServidor();
	
	static Session sessao;
	static Folder indexFolder;

	static List <String> tokensTab = new ArrayList<String>();	
	static List <String> idDocs = new ArrayList<String>();
	static List <String> indexados = new ArrayList<String>();
	static List <String> folderIds = new ArrayList<String>();
	
	static String[] listaEdicoes = new String[55];
	static String[] listData = new String[55];
	
	public static String usuario = "sgj";
    public static String password = "934769386";
//    public static String url = "http://192.168.1.30:8080";
	public static String url = "http://127.0.0.1:8080";
	public static String baseFolder = "/Sites/advocacia/documentLibrary/Secretaria/publicacoes";
	static String idDoc = null;

//	static String queryString = "";
	static String edicaoEscolhida = "";	
	static String token = "";
    static String a = null;
    static String newline = "\n";

	static int k =0;
	static int opcao;
	
	static boolean escolheu = false;

	static JFrame frame = new JFrame("Indexação de Publicações");
	static JPanel controlPane = new JPanel();

	private JButton btn1;
    static JTextArea output;
    static JList<String> list; 
    static JTable table;
	
	static JTextField txt;	

    static ListSelectionModel listSelectionModel;

    public Clipping() {
    	super(new BorderLayout());
    	
    	btn1 = new JButton("Indexar");
    	btn1.addActionListener(this);
    	
    	txt = new JTextField(25); 
		txt.setEnabled(true);
   
        list = new JList<String>(listData);
        listSelectionModel = list.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());

        JScrollPane listPane = new JScrollPane(list);
        
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Build output area.
        output = new JTextArea(1, 10);
        output.setEditable(false);
        
        JScrollPane outputPane = new JScrollPane(output,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        //Do the layout.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        add(splitPane, BorderLayout.CENTER);

        JPanel topHalf = new JPanel();
        topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.PAGE_AXIS));

		// este é o list_view
        JPanel listContainer = new JPanel(new GridLayout(1,1));			// container da list_view
        listContainer.setBorder(BorderFactory.createTitledBorder("Edições localizadas"));
        listContainer.add(listPane);									// listPane é a lista de edicoes
        topHalf.add(listContainer);										// inclusão da list_view no container
        splitPane.add(topHalf);

        topHalf.add(txt);
        
        btn1.setAlignmentY(CENTER_ALIGNMENT);
        topHalf.add(btn1);

        JPanel bottomHalf = new JPanel(new BorderLayout());
        bottomHalf.add(controlPane, BorderLayout.PAGE_START);
        bottomHalf.add(outputPane, BorderLayout.CENTER);
        bottomHalf.setPreferredSize(new Dimension(450, 135));
        splitPane.add(bottomHalf);
    }
    
    public static void main(String[] args){   

        javax.swing.SwingUtilities.invokeLater(new Runnable() {           
        	public void run() {
        		if (!conectaServidor()) {
            		JOptionPane.showMessageDialog(null, "Erro na conexão com o servidor");
        			finalizaProcesso();
        		}
				listaEdicoes();				
            	apresentaJanela();
            }
        });
    }

    public static void listaEdicoes() {	
		Map<String, String> mapEdicoes = new HashMap<String, String>();
		mapEdicoes = conexao.listarEdicoes(sessao, "/Sites/advocacia/documentLibrary/Secretaria/Carregamento");
		carregaEdicoes(mapEdicoes);
    }

	public static boolean conectaServidor() {

		conexao.setUser(usuario);
		conexao.setPassword(password);
		conexao.setUrl(url);
		sessao = InterfaceServidor.serverConnect();
		if (sessao == null) {
			novaMensagem(obtemHrAtual() + "Erro na conexão com o servidor ");
			finalizaProcesso();
			return false;
		}
		return true;
	}
	
    public static void carregaEdicoes(Map<String, ?> edicoes) {   	
    	
    	int ix = 0;
    	Set<String> chaves = edicoes.keySet();
		for (Iterator<String> iterator = chaves.iterator(); iterator.hasNext();)
		{
			String chave = iterator.next();
			if(chave != null) {
				listData[ix] = (edicoes.get(chave).toString());
				folderIds.add(chave);
				ix++;
			}
		}
    }
    
    public static void apresentaJanela() {       
        
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Clipping demo = new Clipping();
        demo.setOpaque(true);
        frame.setContentPane(demo);
        frame.pack();
        frame.setVisible(true);
        txtMensagem("Escolha uma edição");
    }
    
	public static String obtemHrAtual() {	
		
		String hr = "";
		String mn = "";
		String sg = "";
		Calendar data = Calendar.getInstance();
		hr = Integer.toString(data.get(Calendar.HOUR_OF_DAY));
		mn = Integer.toString(data.get(Calendar.MINUTE));
		sg = Integer.toString(data.get(Calendar.SECOND));	
		return completaEsquerda(hr,'0',2) + ":" + completaEsquerda(mn,'0',2) + ":" + completaEsquerda(sg,'0',2) + " - ";
	}
	
	public static String completaEsquerda(String value, char c, int size) {
		
		String result = value;
		while (result.length() < size) {
			result = c + result;
		}
		return result;
	}
	
	public static void finalizaProcesso() {
		
		JOptionPane.showMessageDialog(null, "Fim do Processamento");
        System.exit(0);
	}
	
    class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {             
            opcao = e.getFirstIndex();;
            edicaoEscolhida = listData[opcao];
            if(!escolheu) {
            	txtMensagem("Edicao escolhida: " + edicaoEscolhida);
            	escolheu = true;
            }
        } 
    }
       
    public static void novaMensagem(String mensagem) {    	
    	output.append(mensagem);	
    }
    
    public static void txtMensagem(String mensagem) {
    	txt.setText(mensagem);
    }
    
    public void actionPerformed(ActionEvent evt) {				// botão indexar
		try {
			txtMensagem("Indexando: " + edicaoEscolhida);
			indexar();			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
    
    public static void indexar() throws Exception {
    	
    	String selecionados = null;
    	int indice = 0;
    	List <String> folderOrigem = new ArrayList<String>();
    	
    	novaMensagem(obtemHrAtual() +"Início da indexação");
		
		txt.setText(edicaoEscolhida);
		folderOrigem = conexao.getFolderInfo(sessao, folderIds.get(opcao));
		
		String pastaId = folderOrigem.get(0);
		String pathId = folderOrigem.get(1);
		String descricao = folderOrigem.get(2);
		String pastaNome = folderOrigem.get(3);
		String tribunal = descricao.substring(4, 6);
	/*	
		String strEdicao = descricao.substring(25, 27) + "-";
		strEdicao = strEdicao + descricao.substring(22, 24) + "-";
		strEdicao = strEdicao + descricao.substring(17, 21);	
		
		String strEdicao = descricao.substring(25, 27) + "-";
		strEdicao = strEdicao + descricao.substring(22, 24) + "-";
		strEdicao = strEdicao + descricao.substring(17, 21);

	*/	
		String strEdicao = descricao.substring(17, 27);
				
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        
        Date edicao = sdf.parse(strEdicao);      

    	String tokenTribunal = conexao.obtemTribunal(sessao, pastaId);
		tokensTab = conexao.carregaTokens(sessao, tokenTribunal); 	
		if(tokensTab.size() == 0) {
        	novaMensagem(obtemHrAtual() + "Não existe tokens para o TRT" + tokenTribunal);
            finalizaProcesso();
        }
       
        indexados.clear();

		while (indice < tokensTab.size()-1) {			
			selecionados = "SELECT d.cmis:objectId FROM sij:documento AS d JOIN sij:publicacao AS w ON d.cmis:objectId = w.cmis:objectId WHERE contains(d,'\\'" + tokensTab.get(indice) + "\\'') AND in_folder(d,'" + pastaId + "') AND w.sij:pubTribunal = '" + tribunal + "'";			
			idDocs.clear();
			idDocs = (conexao.localizaEditais(sessao, selecionados));
			if(idDocs.size() >0) {
				novaMensagem(obtemHrAtual() + "Localizado Token: " + tokensTab.get(indice));
				for (int i = 0; i <= idDocs.size()-1; i++) {
					InterfaceServidor.indexaEdital(sessao, idDocs.get(i), tokensTab.get(indice));
					if(!indexados.contains(idDocs.get(i))) {
						indexados.add(idDocs.get(i));
					}	
				}
			}		
			indice++;
			k++;
		}
		
		// -------------------------------------------------------------------------------------------
		
		novaMensagem(obtemHrAtual() + "Início da movimentação das publicações");
		
		if(indexados != null) {
			
			indexFolder = InterfaceServidor.verificaEdtFolder(sessao, baseFolder, pastaNome + "x", descricao + " indexado", tribunal, edicao);
			
			for (int i = 0; i <= indexados.size()-1; i++) {
				InterfaceServidor.moveEditalIndexado(sessao, indexados.get(i), pathId, indexFolder);
				novaMensagem(obtemHrAtual()+ "Publicação  " + indexados.get(i) + " movida com sucesso");
			}		
		}
		novaMensagem(obtemHrAtual() + "Processo de indexação concluído");
		escolheu = false;
		finalizaProcesso();
    }
}