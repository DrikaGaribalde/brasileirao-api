package br.com.phc.brasileiraoapi.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.phc.brasileiraoapi.dto.PartidaGoogleDto;

@Service
public class ScrapingUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String BASE_URL_GOOGLE = "https://www.google.com.br/search?q=";
	private static final String COMPLEMENTO_URL_GOOGLE = "&hl=pt-BR";

	private static final String ITEM_GOL = "div[class=imso_gs__gs-r]";
	private static final String DIV_GOLS_EQUIPE_CASA = "div[class=imso_gs__tgs imso_gs__left-team]";
	private static final String DIV_GOLS_EQUIPE_VISITANTE = "div[class=imso_gs__tgs imso_gs__right-team]";

	private static final String DIV_PLACAR_EQUIPE_CASA = "div[class=imso_mh__l-tm-sc imso_mh__scr-it imso-light-font]";
	private static final String DIV_PLACAR_EQUIPE_VISITANTE = "div[class=imso_mh__r-tm-sc imso_mh__scr-it imso-light-font]";

	private static final String ITEM_LOGO = "img[class=imso_btl__mh-logo]";
	private static final String DIV_DADOS_CASA = "div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String DIV_DADOS_VISITANTE = "div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]";

//	private static final String CASA ="casa";
//	private static final String VISITANTE = "visitante";

	private static final String DIV_PARTIDA_ANDAMENTO = "div[class=imso_mh__lv-m-stts-cont]";
	private static final String DIV_PARTIDA_ENCERRADA = "span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]";

	private static final String HTTPS = "https:";
	private static final String SRC = "src";
	private static final String SPAN = "span";
	private static final String PENALTIS = "Pênaltis";

//	public static void main(String[] args) {
//
//		// 30-12-2021-arouca-x-braga
//		// Getafe+x+Real+Madrid
//		// Crystal+Palace+x+West+Ham
//		//Everton+x+Brighton
//		//atlético+mineiro+x+goias
//		// Segunda aba
//				
//		ScrapingUtil scraping = new ScrapingUtil();
//		String url = scraping.montarUrlGoogle("Getafe", "Real Madrid");
//		scraping.obtemInformacoesPartida(url);
//
//	}
	//teste branch api-web
	public PartidaGoogleDto obtemInformacoesPartida(String url) {
		PartidaGoogleDto partida = new PartidaGoogleDto();
		//
		Document document = null;

		try {
			document = Jsoup.connect(url).get();

			// recuperar informações da página
			String title = document.title();
			LOGGER.info("Título da Página {}", title);

			StatusPartida statusPartida = obtemStatusPartida(document);
			partida.setStatusPartida(statusPartida.toString());
			LOGGER.info(statusPartida.toString());

			if (statusPartida != StatusPartida.PARTIDA_NAO_INICIADA) {
				String tempoPartida = obtemTempoPartida(document);
				partida.setTempoPartida(tempoPartida);
				LOGGER.info("Tempo Partida: {}", tempoPartida);
				//
				// Placar
				Integer placarEquipeCasa = recuperarPlacarEquipe(document, DIV_PLACAR_EQUIPE_CASA);
				partida.setPlacarEquipeCasa(placarEquipeCasa);
				LOGGER.info("Placar Casa: {}", placarEquipeCasa.toString());

				Integer placarEquipeVisitante = recuperarPlacarEquipe(document, DIV_PLACAR_EQUIPE_VISITANTE);
				partida.setPlacarEquipeVisitante(placarEquipeVisitante);
				LOGGER.info("Placar Visitante: {}", placarEquipeVisitante.toString());

				// Gols das equipes
				String golsEquipeCasa = recuperarGolsEquipe(document, DIV_GOLS_EQUIPE_CASA);
				partida.setGolsEquipeCasa(golsEquipeCasa);
				LOGGER.info("Gols EquipeCasa: {}", golsEquipeCasa);

				String golsEquipeVisitante = recuperarGolsEquipe(document, DIV_GOLS_EQUIPE_VISITANTE);
				partida.setGolsEquipeVisitante(golsEquipeVisitante);
				LOGGER.info("Gols EquipeVisitante: {}", golsEquipeVisitante);
			}
			//
			String nomeEquipeCasa = recuperaNomeEquipe(document, DIV_DADOS_CASA);
			partida.setNomeEquipeCasa(nomeEquipeCasa);
			LOGGER.info("Nome Equipe da Casa: {}", nomeEquipeCasa);

			String nomeEquipeVisitante = recuperaNomeEquipe(document, DIV_DADOS_VISITANTE);
			partida.setNomeEquipeVisitante(nomeEquipeVisitante);
			LOGGER.info("Nome Equipe Visitante: {}", nomeEquipeVisitante);

			// Recuperar as logos
			String urlLogoEquipeCasa = recuperarLogoEquipe(document, DIV_DADOS_CASA);
			partida.setUrlLogoEquipeCasa(urlLogoEquipeCasa);
			LOGGER.info("urlLogo Equipe da Casa: {} ", urlLogoEquipeCasa);

			String urlLogoEquipeVisitante = recuperarLogoEquipe(document, DIV_DADOS_VISITANTE);
			partida.setUrlLogoEquipeVisitante(urlLogoEquipeVisitante);
			LOGGER.info("urlLogo Equipe Visistante: {} ", urlLogoEquipeVisitante);
			
			return partida;

		} catch (IOException e) {
			LOGGER.error("Erro ao tentar conectar no Google com Jsoup!!! {}", e.getMessage());
		}

		return null;
	}

	// pegando dados da página
	public StatusPartida obtemStatusPartida(Document document) {
		// situações
		// Partida não iniciada
		// Partida iniciada || jogo rolando || intervalo
		// Partida encerrada
		// Penalidades

		StatusPartida statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		boolean isTempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).isEmpty();
		if (!isTempoPartida) {
			String tempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ANDAMENTO;
			if (tempoPartida.contains(PENALTIS)) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
		}
		isTempoPartida = document.select(DIV_PARTIDA_ENCERRADA).isEmpty();
		if (!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}
		return statusPartida;
	}

	public String obtemTempoPartida(Document document) {
		String tempoPartida = null;
		// jogo rolando ou intervalo ou penalidades
		boolean isTempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).first().text();
		}
		isTempoPartida = document.select(DIV_PARTIDA_ENCERRADA).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(DIV_PARTIDA_ENCERRADA).first().text();
		}

		return corrigeTempoPartida(tempoPartida);
	}

	public String corrigeTempoPartida(String tempo) {
		if (tempo.contains("'")) {
			return tempo.replace("'", " min");
		} else {
			return tempo;
		}
	}

	// nomeEquipe ok
	public String recuperaNomeEquipe(Document document, String itemHtml) {
		Element elemento = document.selectFirst(itemHtml);
		String nomeEquipe = elemento.select(SPAN).text();
		return nomeEquipe;
	}

	// urlLogo ok
	public String recuperarLogoEquipe(Document document, String itemHtml) {
		Element elemento = document.selectFirst(itemHtml);
		String urlLogo = HTTPS + elemento.select(ITEM_LOGO).attr(SRC);
		return urlLogo;
	}

	// placar ok
	public Integer recuperarPlacarEquipe(Document document, String itemHtml) {
		String placarEquipe = document.selectFirst(itemHtml).text();
		return formataPlacar(placarEquipe);
	}

	// Recuperar Gols ok
	public String recuperarGolsEquipe(Document document, String itemHtml) {
		List<String> golsEquipe = new ArrayList<>();

		Elements elementos = document.select(itemHtml).select(ITEM_GOL);
		for (Element e : elementos) {
			String infoGol = e.select(ITEM_GOL).text();
			golsEquipe.add(infoGol);
		}
		return String.join(", ", golsEquipe);
	}

	// Tratamento caso o placar seja nulo
	public Integer formataPlacar(String placar) {
		Integer valor;
		try {
			valor = Integer.parseInt(placar);
		} catch (Exception e) {
			valor = 0;
		}
		return valor;
	}

	// Montar Url
	public String montarUrlGoogle(String nomeEquipeCasa, String nomeEquipeVisitante) {
		try {
			String equipeCasa = nomeEquipeCasa.replace(" ", "+").replace("-", "+");
			String equipeVisitante = nomeEquipeVisitante.replace(" ", "+").replace("-", "+");

			return BASE_URL_GOOGLE + equipeCasa + "X" + equipeVisitante + COMPLEMENTO_URL_GOOGLE;
		} catch (Exception e) {
			LOGGER.error("Erro na url: {}", e.getMessage());
		}

		return null;

	}

}
