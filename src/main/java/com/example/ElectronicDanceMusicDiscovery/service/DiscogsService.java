package com.example.ElectronicDanceMusicDiscovery.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class DiscogsService {

    private final String SEARCH_URL = "https://api.discogs.com/database/search";
    private final String BASE_URL = "https://api.discogs.com/releases/";
    private final String API_TOKEN = "FEfmhYboeTNVAmTEnFOCFVkUZPwNnJQFaUKOglBF"; // Replace with your Discogs API token
    private final RestTemplate restTemplate;

    @Autowired
    public DiscogsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Method to get a list of Release IDs based on a search query
    public List<Integer> getListOfReleaseId(String musicGenreStyle) {

        Random pageRandomizer = new Random();
        int minPage = 1;
        int maxPage = getMaxPage(musicGenreStyle);

        int page = pageRandomizer.nextInt(maxPage-minPage) + minPage;

        String url = SEARCH_URL + "?style=" + musicGenreStyle + "&page=" + page + "&type=release&token=" + API_TOKEN;
        System.out.println(url);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            // Extracting the list of releases from the response
            List<Map<String, Object>> releases = (List<Map<String, Object>>) response.getBody().get("results");

            // Extracting release IDs into a list
            return releases.stream()
                    .map(release -> (Integer) release.get("id"))
                    .toList();  // Java 16+ or use Collectors.toList() for earlier versions
        } catch (HttpClientErrorException e) {
            e.printStackTrace(); // Handle error
            return null;
        }
    }

    public int getMaxPage(String musicGenreStyle) {

        String url = SEARCH_URL + "?style=" + musicGenreStyle + "&type=release&token=" + API_TOKEN;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            // Extract pagination info
            Map<String, Object> pagination = (Map<String, Object>) response.getBody().get("pagination");

            // Get the total items and items per page from pagination
            int totalItems = (int) pagination.get("items");
            int perPage = (int) pagination.get("per_page");

            // Calculate total pages
            int totalPages = (int) Math.ceil((double) totalItems / perPage);

            // Max total pages can be 200 according to discogs api
            if (totalPages >= 200){
                return 200;
            }

            return totalPages;
        } catch (HttpClientErrorException e) {
            e.printStackTrace(); // Handle error
            return 0;
        }
    }

    public Map<String, Object> getReleaseData(int releaseId) {
        String url = BASE_URL + releaseId + "?token=" + API_TOKEN;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            e.printStackTrace(); // Handle error
            return null;
        }
    }
}
