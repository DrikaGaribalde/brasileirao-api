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
		
		String url = BASE_URL_GOOGLE + "palmeiras+x+corinthians+08/08/2020" + COMPLEMENTO_URL_GOOGLE;
		ScrapingUtil scraping = new ScrapingUtil();
		scraping.obtemInformacoesPartida(url);

	}
	
	public PartidaGoogleDto obtemInformacoesPartida(String url) {
		PartidaGoogleDto partida = new PartidaGoogleDto();
		//
		Document document = null;
		
		try {
			document = Jsoup.connect(url).get();
			//recuperar informações da página
			String title = document.title();
			LOGGER.info("Título da Página {}", title);
		} catch (IOException e) {
			LOGGER.error("Erro ao tentar conectar no Google com Jsoup!!! {}", e.getMessage());
		}
		
		return partida;
	}

}
