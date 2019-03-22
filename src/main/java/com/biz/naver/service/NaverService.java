package com.biz.naver.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import com.biz.naver.config.NaverClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NaverService {
	
	String movieURL = "https://openapi.naver.com/v1/search/movie.json";
	String bookURL = "https://openapi.naver.com/v1/search/book.json";
	String newsURL = "https://openapi.naver.com/v1/search/news.json";
	private Log logger = LogFactory.getLog(this.getClass());

	/*
	 * JSON형태의 문자열을 매개변수로 받아서
	 * Jsonobject로 parsing한 후 
	 * List<VO>같은 java Object형으로 변환하여
	 * controller로 return하도록 작성
	 */
	/*
	 * JsonString -> Jsonobject
	 * 전체 필요한 항목 추출
	 * get을 이용해서 key값(total)을 입력하면 해당되는 항목이 추출해서 나옴
	 * jsonobject를 tostring으로 해서 문자열로 바꾸고
	 * 다시 숫자열로 바꾸라는 말.
	 * 그럼 total의 값이 나옴.
	 */
	
	public JSONArray getObject(String jsonString) {
		/*
		 * jsonString을 JSONObject로 변환 작업 실행
		 * 1. JSONParser 객체 생성
		 * 
		 */
		JSONParser jp = new JSONParser();
		
		// 2. JSONParser 객체를 경유해서 문자열을 JSONObject로 일단 변환
		JSONObject jo = null;
		
		try {
			jo = (JSONObject) jp.parse(jsonString);
			long longTotal = (long) jo.get("total");
			String lastDate = (String)jo.get("lastBuildDate");
			
			
			log.debug("요청한 시각:" + lastDate.toString());
			log.debug("수신한 데이터 개수:" + longTotal);
			
			/*
			 * 문자열 중에서 도서정보가 포함된 영역만 추출
			 * 도서정보가 포함된 영역의 key items
			 */
			// items 항목 부분만 추출해서 JsonArray로 변환
			JSONArray items = (JSONArray) jo.get("items");
			int itemsLen = items.size();
			
			for(int i = 0; i < itemsLen; i++) {
				log.debug(i+ "번째 데이터" + items.get(i));
			}
			
			return items;
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	// 도서정보 검색
	public String getString(String cate, String searchText) {
		
		logger.debug("반갑습니다");
		
		// id와 key 세팅
		String clientId = NaverClient.ID;
		String clientKey = NaverClient.KEY;
		
		// 검색문자열을 하나 선언
		// String text = "자바";
		
		// 검색문자열을 Naver로 보내기전에 Encoding을 실시
		
		try {
			String text = URLEncoder.encode(searchText,"UTF-8");
			String apiURL = "https://openapi.naver.com/v1/search/book.json";
			if(cate.equalsIgnoreCase("MOVIE")) {
				apiURL = movieURL;
			}
			if(cate.equalsIgnoreCase("NEWS")) {
				apiURL = newsURL;
			}
			apiURL += "?query=" + text;
			
			// URL 객체로 생성
			URL url = new URL(apiURL);
			
			// HttpRequest로 변환
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			// 접속정보를 setting
			conn.setRequestMethod("GET");
			conn.setRequestProperty("X-Naver-Client-Id", clientId);
			conn.setRequestProperty("X-Naver-Client-Secret", clientKey);
			
			// 네이버에게 요청을 보내서 내 요청에 응답할 수 있느냐라고 묻는것.
			int resCode = conn.getResponseCode();
			
			BufferedReader buffer;
			if(resCode == 200) {
				// 데이터를 수신할 준비
				// inputStreamreader는 인터넷을 통해 conn에서부터 연결된 객체를 가져오는것.
				InputStreamReader is = new InputStreamReader(conn.getInputStream());
				buffer = new BufferedReader(is);
			}else {
				// 오류가 무엇인지 분석
				InputStreamReader is = new InputStreamReader(conn.getErrorStream());
				buffer = new BufferedReader(is);
			}
			
			String reader = "";
			String readStrings ="";
			while(true) {
				
				reader = buffer.readLine();
				if(reader == null) break;
				log.debug(reader);
				readStrings += reader; // 한개의문자열로붙여서읽어라..
			}
			
			buffer.close();
			return readStrings; // controller로 return
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
