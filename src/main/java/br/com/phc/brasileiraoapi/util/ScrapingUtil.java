package br.com.phc.brasileiraoapi.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
		// Segunda aba
		String url = BASE_URL_GOOGLE + "Getafe+x+Real+Madrid" + COMPLEMENTO_URL_GOOGLE;
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

	//
	private String recuperarLogoEquipeCasa(Document document) {
		Element elemento = document.selectFirst("div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]");
		String urlLogo = "https:" + elemento.select("img[class=imso_btl__mh-logo]").attr("src");
		return urlLogo;
	}

	public String recuperarLogoEquipeVisitante(Document document) {
		Element elemento = document.selectFirst("div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]");
		String urlLogo = "https:" + elemento.select("img[class=imso_btl__mh-logo]").attr("src").toString(); 
		return urlLogo;
	}
//String base64 = out.toString(StandardCharsets.US_ASCII);
}
