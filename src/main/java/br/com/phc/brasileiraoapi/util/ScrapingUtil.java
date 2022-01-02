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

import br.com.phc.brasileiraoapi.dto.PartidaGoogleDto;

public class ScrapingUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String BASE_URL_GOOGLE = "https://www.google.com.br/search?q=";
	private static final String COMPLEMENTO_URL_GOOGLE = "&hl=pt-BR";

	public static void main(String[] args) {

		// 30-12-2021-arouca-x-braga
		// Getafe+x+Real+Madrid
		// Crystal+Palace+x+West+Ham
		//Everton+x+Brighton
		// Segunda aba
		String url = BASE_URL_GOOGLE + "Southampton+x+Newcastle" + COMPLEMENTO_URL_GOOGLE;
		ScrapingUtil scraping = new ScrapingUtil();
		scraping.obtemInformacoesPartida(url);

	}

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
			LOGGER.info(statusPartida.toString());

			if (statusPartida != StatusPartida.PARTIDA_NAO_INICIADA) {
				String tempoPartida = obtemTempoPartida(document);
				LOGGER.info("Tempo Partida: {}", tempoPartida);
			}
			//
			String nomeEquipeCasa = recuperaNomeEquipeCasa(document);
			LOGGER.info("Nome Equipe da Casa: {}", nomeEquipeCasa);

			String nomeEquipeVisitante = recuperaNomeEquipeVisitante(document);
			LOGGER.info("Nome Equipe Visitante: {}", nomeEquipeVisitante);

			// Recuperar as logos
			String urlLogoEquipeCasa = recuperarLogoEquipeCasa(document);
			LOGGER.info("urlLogo Equipe da Casa: {} ", urlLogoEquipeCasa);

			String urlLogoEquipeVisitante = recuperarLogoEquipeVisitante(document);
			LOGGER.info("urlLogo Equipe Visistante: {} ", urlLogoEquipeVisitante);

			// Placar
			Integer placarEquipeCasa = recuperarPlacarEquipeCasa(document);
			LOGGER.info("Placar Casa: {}", placarEquipeCasa.toString());

			Integer placarEquipeVisitante = recuperarPlacarEquipeVisitante(document);
			LOGGER.info("Placar Visitante: {}", placarEquipeVisitante.toString());

			// Gols das equipes
			String golsEquipeCasa = recuperarGolsEquipeCasa(document);
			LOGGER.info("Gols EquipeCasa: {}", golsEquipeCasa);

			String golsEquipeVisitante = recuperarGolsEquipeVisitante(document);
			LOGGER.info("Gols EquipeVisitante: {}", golsEquipeVisitante);

		} catch (IOException e) {
			LOGGER.error("Erro ao tentar conectar no Google com Jsoup!!! {}", e.getMessage());
		}

		return partida;
	}

	// pegando dados da página
	public StatusPartida obtemStatusPartida(Document document) {
		// situações
		// Partida não iniciada
		// Partida iniciada || jogo rolando || intervalo
		// Partida encerrada
		// Penalidades
		StatusPartida statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		boolean isTempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").isEmpty();
		if (!isTempoPartida) {
			String tempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ANDAMENTO;
			if (tempoPartida.contains("Pênaltis")) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
		}
		isTempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").isEmpty();
		if (!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}
//		LOGGER.info(statusPartida.toString());
		return statusPartida;
	}

	public String obtemTempoPartida(Document document) {
		String tempoPartida = null;
		// jogo rolando ou intervalo ou penalidades
		boolean isTempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").first().text();
		}
		isTempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").first()
					.text();
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

	// nomeEquipe
	public String recuperaNomeEquipeCasa(Document document) {
		Element elemento = document.selectFirst("div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]");
		String nomeEquipe = elemento.select("span").text();
		return nomeEquipe;
	}

	public String recuperaNomeEquipeVisitante(Document document) {
		Element elemento = document.selectFirst("div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]");
		String nomeEquipe = elemento.select("span").text();
		return nomeEquipe;
	}

	// urlLogo
	public String recuperarLogoEquipeCasa(Document document) {
		Element elemento = document.selectFirst("div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]");
		String urlLogo = "https:" + elemento.select("img[class=imso_btl__mh-logo]").attr("src");
		return urlLogo;
	}

	public String recuperarLogoEquipeVisitante(Document document) {
		Element elemento = document.selectFirst("div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]");
		String urlLogo = "https:" + elemento.select("img[class=imso_btl__mh-logo]").attr("src");
		return urlLogo;
	}

	// placar
	public Integer recuperarPlacarEquipeCasa(Document document) {
		String placarEquipe = document.selectFirst("div[class=imso_mh__l-tm-sc imso_mh__scr-it imso-light-font]")
				.text();
		//return Integer.valueOf(placarEquipe);
		return formataPlacar(placarEquipe);
	}

	public Integer recuperarPlacarEquipeVisitante(Document document) {
		String placarEquipe = document.selectFirst("div[class=imso_mh__r-tm-sc imso_mh__scr-it imso-light-font]")
				.text();
		//return Integer.valueOf(placarEquipe);
		return formataPlacar(placarEquipe);
	}

	// Recuperar Gols
	public String recuperarGolsEquipeCasa(Document document) {
		List<String> golsEquipe = new ArrayList<>();

		Elements elementos = document.select("div[class=imso_gs__tgs imso_gs__left-team]")
				.select("div[class=imso_gs__gs-r]");
		for (Element e : elementos) {
			String infoGol = e.select("div[class=imso_gs__gs-r]").text();
			golsEquipe.add(infoGol);
		}
		return String.join(", ", golsEquipe);
	}

	public String recuperarGolsEquipeVisitante(Document document) {
		List<String> golsEquipe = new ArrayList<>();

		Elements elementos = document.select("div[class=imso_gs__tgs imso_gs__right-team]")
				.select("div[class=imso_gs__gs-r]");
		elementos.forEach(item -> {
			String infoGol = item.select("div[class=imso_gs__gs-r]").text();
			golsEquipe.add(infoGol);
		});
		return String.join(", ", golsEquipe);
	}
	
	//Tratamento caso o placar seja nulo
	public Integer formataPlacar(String placar) {
		Integer valor;
		try {
			valor = Integer.parseInt(placar);
		} catch (Exception e) {
			valor = 0;
		}
		return valor;
	}
	

}
