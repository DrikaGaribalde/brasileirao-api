package br.com.phc.brasileiraoapi.util;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.phc.brasileiraoapi.dto.PartidaGoogleDto;

public class ScrapingUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String BASE_URL_GOOGLE = "https://www.google.com.br/search?q=";
	private static final String COMPLEMENTO_URL_GOOGLE = "&hl=pt-BR";

	public static void main(String[] args) {

		// 30-12-2021-arouca-x-braga
		// Segunda aba
		String url = BASE_URL_GOOGLE + "Crystal+Palace+x+West+Ham"
				+ COMPLEMENTO_URL_GOOGLE;
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
			
			String tempoPartida = obtemTempoPartida(document);
			LOGGER.info("Tempo Partida: {}", tempoPartida);

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
		//jogo rolando ou intervalo ou penalidades
		boolean isTempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").first().text();		
		}
		isTempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").first().text();
		}
	
		return corrigeTempoPartida(tempoPartida);
	}
	
	public String corrigeTempoPartida(String tempo) {
		if(tempo.contains("'")) {
			return tempo.replace("'", " min");
		}else {
			return tempo;
		}
	}
}
