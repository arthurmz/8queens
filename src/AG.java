import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * Algoritmo genético para o problema das 8 rainhas
 * O problema das oito rainhas consiste em dispor as 8 rainhas sobre
 * o tabuleiro de xadrez, de forma que nenhuma ataque outra.
 * Portanto não pode haver duas rainhas numa mesma linha horizontal, vertical
 * ou diagonal.
 * 
 * @author Arthur
 *
 */
public class AG {
	static int TAMANHO_POP = 15;
	/** estados válidos de um gene {0, 1, 2, 3, 4, 5, 6, 7} */
	static int ESTADOS_POSSIVEIS = 8;
	/** probabilidade de dois pais gerarem dois filhos, 
	 * caso contrário, os indivíduos resultantes são os dois pais.*/
	static float PROB_RECOMBINACAO = 0.5f;
	/** probabilidade de um gene de um indivíduo sofrer mutação (inversão para um valor válido) 
	 * Nesse caso a mutação é aplicada nos 3 primeiros bits (range de 0 a 7) */
	static float PROB_MUTACAO = 0.01f;
	
	static int PONTO_RECOMBINACAO = 5;
	
	static Random rand = new Random();
	
	public static void main(String[] args) {
		/**
		 * O indivíduo é um array de bytes.
		 * Duas estruturas de população são usadas de forma alternada.
		 * assim quando uma população de pais preencher a segunda população de filhos, 
		 * na próxima iteração a população de pais é a segunda e vice versa.
		 * Motivo: não precisa alocar memória o tempo todo
		 */
		List<byte[]> populacao1 = new ArrayList<byte[]>();
		List<byte[]> populacao2 = new ArrayList<byte[]>();
		List<List<byte[]>> populacoes = new ArrayList<List<byte[]>>();
		
		populacoes.add(populacao1);
		populacoes.add(populacao2);
		
		int pAtual = 0;
		iniciarPopulacao(populacoes.get(0));
		populacao1.set(0, new byte[]{7, 1, 4, 2, 0, 6, 3, 5});// testando o critério de parada (comentar para rodar livremente)
		
		while (regraParada(populacoes.get(pAtual))){
			gerarFilhos(populacoes.get(pAtual), populacoes.get(pAtual==1? 0:1));
			pAtual = pAtual==1? 0:1;
		}
		
		mostrarPopulacao(populacoes.get(pAtual));
		return;
		
	}
	
	private static void mostrarPopulacao(List<byte[]> list) {
		for (byte[] indv : list){
			if (avaliarIndividuo(indv) == 100){
				
				System.out.print("Indivíduo solução: {");
				for (byte value : indv){
					System.out.print(","+value);
				}
				System.out.print("}");
			}
		}
		
	}

	private static void iniciarPopulacao(List<byte[]> populacao){
		for (int i = 0; i < TAMANHO_POP; i++){
			byte[] individuo = new byte[8];
			for (int j = 0; j < 8; j++){
				individuo[j] = (byte) rand.nextInt(ESTADOS_POSSIVEIS);//8 genes possíveis
			}
			populacao.add(individuo);
		}
	}
	
	/**
	 * Para quando um dos indivíduos atingir o objetivo.
	 * @param populacao
	 * @return
	 */
	private static boolean regraParada(List<byte[]> populacao){
		for (byte[] indv : populacao){
			if (avaliarIndividuo(indv) == 100)
				return false;
		}
		return true;//continue procurando
	}
	
	/**
	 * Verifica o somatório da quantidade de rainhas que atacam uma determinada rainha.
	 * para todas as rainhas.
	 * 
	 * Pela a escolha da forma de implementação, é impossível duas rainhas ocuparem a mesma coluna.
	 * 
	 * Um indivíduo solução do problema possui aptidão 100
	 * @return
	 */
	private static int avaliarIndividuo(byte[] individuo){
		int score = 100;//quanto mais rainhas com ataque, menor o score (o número máximo de subtrações é 8x8 = 64.)
		for (int i = 0; i < 8; i++){
			int linha = individuo[i];
			int coluna = i;
			for (int j = 0; j < 8; j++){//percorre todos os elementos do array, menos o próprio avaliado
				if (j == i) continue;
				if (individuo[j] == linha)
					score--;
				//para cada rainha que não é à rainha atual, existem duas posições relativas em que elas podem se atacar diagonalmente.
				int linhaVizinho = individuo[j];
				int colunaVizinho = j;
				
				int distanciaColuna = Math.abs(colunaVizinho - coluna);
				int distanciaLinha = Math.abs(linhaVizinho - linha);
				if (distanciaColuna == distanciaLinha)
					score--;
			}
			
		}
		return score;
		
	}
	
	/**
	 * Gera uma nova populacao, aplicando operadores de recombinação e mutação
	 * 
	 * Executa a roleta, selecionando dois pais aleatórios, aplica a probabilidade de recombinação e gera um filho.
	 * roda isso até obter a população de filhos final
	 * @param pais
	 * @param filhos
	 */
	private static void gerarFilhos(List<byte[]> pais, List<byte[]> filhos){
		filhos.clear();
		int[] pontuacao = new int[pais.size()];
		int total = 0;
		
		for (int i = 0; i < pais.size(); i++){
			pontuacao[i] = avaliarIndividuo(pais.get(i));
			total+= pontuacao[i];
		}
		
		int qtdFilhos = 0;
		while (filhos.size() < TAMANHO_POP){
			byte[] pai = roleta(pais, total, pontuacao);
			byte[] mae = roleta(pais, total, pontuacao);
			
			int recombinar = rand.nextInt(100);
			if (recombinar < PROB_RECOMBINACAO*100){
				byte[] filho1 = recombinar(pai, mae);
				aplicarMutacao(filho1);
				filhos.add(filho1);
				
				byte[] filho2 = recombinar(mae, pai);
				aplicarMutacao(filho2);
				filhos.add(filho2);
			}
			else{
				filhos.add(pai);
				filhos.add(mae);
			}
			descartarDuplicatas(filhos);
		}
		
		if (filhos.size() > TAMANHO_POP){
		      filhos.subList(TAMANHO_POP, filhos.size()).clear();
		}
		
		
	}

	private static void descartarDuplicatas(List<byte[]> filhos) {
		Set<byte[]> s = new LinkedHashSet<byte[]>(filhos);
		filhos.clear();
		filhos.addAll(s);
	}

	/**
	 * Aplica a mutação no filhos na taxa definida.
	 * A mutação realizada é só para indivíduos viáveis.
	 * isto é, para valores de genes dentro do espaço (0...7)
	 * @param filhos
	 */
	private static void aplicarMutacao(byte[] filhos) {
		int valor = (int) (PROB_MUTACAO * 100);
		
		
		for (int i = 0; i < filhos.length; i++){
			for (int j = 0; j < 3; j++){
				int mutar = rand.nextInt(100);
				//100 - 4
				//010 - 2
				//001 - 1
				
				if (mutar <= valor){
					if (j == 0)
						filhos[i] = (byte) ((~filhos[i]) & 0x4);
					else if (j == 1)
						filhos[i] = (byte) ((~filhos[i]) & 0x2);
					else if (j == 2)
						filhos[i] = (byte) ((~filhos[i]) & 0x1);
				}
			}
		}
	}

	/**
	 * Aplica o crossover entre o pai e a mãe, gerando um filho
	 * @param mae
	 * @param pai
	 * @return
	 */
	private static byte[] recombinar(byte[] pai, byte[] mae) {
		//Unico ponto de recombinação
		byte[] filho = new byte[8];
		
		for (int i = 0; i < pai.length; i++){
			if (i < PONTO_RECOMBINACAO)
				filho[i] = pai[i];
			else
				filho[i] = mae[i];
		}
		
		return filho;
	}

	/**
	 * Retorna um indivíduo aleatório da população de pais, seguindo a probabilidade que
	 * cada um tem na roleta.
	 * @param pais
	 * @param total
	 * @param pontuacao
	 * @return 
	 */
	private static byte[] roleta(List<byte[]> pais, int total, int[] pontuacao) {
		int posicao = rand.nextInt(100);
		
		int[] posicaoInicioIndividuo = new int[pais.size()+1];//posição de início do indivídio 'i' na roleta virtual
		
		int porcentagem = 0;
		for (int i = 0; i < pais.size()-1; i++){
			porcentagem += (pontuacao[i]* 100/total);
			//Como o primeiro indivíduo sempre começa na posição zero, estamos descobrindo a posição do PRÓXIMO.
			posicaoInicioIndividuo[i+1] = porcentagem;
		}
		
		posicaoInicioIndividuo[10] = 100;//usado para fechar a execução desse loop
		for (int i = 0; i < posicaoInicioIndividuo.length-1; i++){
			if (posicaoInicioIndividuo[i+1] >= posicao)
				return pais.get(i);
		}
		
		return null;
		
	}

}


