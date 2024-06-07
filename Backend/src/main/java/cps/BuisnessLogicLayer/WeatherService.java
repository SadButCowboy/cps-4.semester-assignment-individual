package cps.BuisnessLogicLayer;

import cps.DataAccessLayer.WeatherData;
import cps.DataAccessLayer.WeatherDataRepository;
import cps.singleton.ConfigurationManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WeatherService {

    private final WeatherDataRepository weatherDataRepository;
    private final List<String> parameterIds = Arrays.asList("precip_past10min", "temp_dry", "humidity", "wind_speed", "wind_dir", "cloud_cover", "sun_last10min_glob", "radia_glob");

    public WeatherService(WeatherDataRepository weatherDataRepository) {
        this.weatherDataRepository = weatherDataRepository;
    }

    @Scheduled(fixedRate = 600_000)
    public void storeValuesInDB() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        String apiKey = configManager.getProperty("api.key");

        URI uri = URI.create("https://dmigw.govcloud.dk/v2/metObs/collections/observation/items?period=latest-10-minutes&bbox=9.938,55.2629,10.8478,55.6235&api-key=" + apiKey);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response;

        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        HashMap<String, Double> valueMap = new HashMap<>();
        JSONArray featureArray = new JSONObject(response.body()).getJSONArray("features");
        for (int i = 0; i < featureArray.length(); i++) {
            String parameterId = featureArray.getJSONObject(i).getJSONObject("properties").getString("parameterId");
            if (!parameterIds.contains(parameterId)) continue;
            double val = featureArray.getJSONObject(i).getJSONObject("properties").getDouble("value");
            valueMap.put(parameterId, val);
        }

        String time = featureArray.getJSONObject(0).getJSONObject("properties").getString("observed");
        Timestamp timestamp = getTimestampFromString(time);
        WeatherData latestWeatherData = getLatestValueFromDB();
        if (latestWeatherData != null && timestamp.equals(latestWeatherData.getTimestamp())) {
            return;
        }

        WeatherData weatherData = new WeatherData();
        weatherData.setRain(valueMap.getOrDefault("precip_past10min", 0.0));
        weatherData.setTimestamp(timestamp);
        weatherData.setTemperature(valueMap.getOrDefault("temp_dry", 0.0));
        weatherData.setHumidity(valueMap.getOrDefault("humidity", 0.0));
        weatherData.setSolarRad(valueMap.getOrDefault("radia_glob", 0.0));
        weatherData.setCloudCoverage(valueMap.getOrDefault("cloud_cover", 0.0));
        weatherData.setSunMin(valueMap.getOrDefault("sun_last10min_glob", 0.0));
        weatherData.setWindDirection(valueMap.getOrDefault("wind_dir", 0.0));
        weatherData.setWindSpeed(valueMap.getOrDefault("wind_speed", 0.0));

        weatherDataRepository.save(weatherData);
    }

    public WeatherData getLatestValueFromDB() {
        return weatherDataRepository.findFirstByTimestampLessThanEqualOrderByTimestampDesc(Timestamp.valueOf(LocalDateTime.now()));
    }

    public WeatherData getValueFromTimestamp(Timestamp timestamp) {
        return weatherDataRepository.findFirstByTimestampLessThanEqualOrderByTimestampDesc(timestamp);
    }

    public Timestamp getTimestampFromString(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Date date = sdf.parse(time);
            return new Timestamp(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDateTime() {
        Timestamp ts = getLatestValueFromDB().getTimestamp();
        Date date = new Date(ts.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        return sdf.format(date);
    }

    public Timestamp[] getAllTimestamps() {
        return weatherDataRepository.findTimestamps();
    }

    public double calculateOverallWeather() {
        WeatherData wd = getLatestValueFromDB();
        if (wd.getRain() > 1) return 1;
        else if (wd.getWindSpeed() > 8) return 2;
        else if (wd.getCloudCoverage() > 60) return 3;
        else return 4;
    }
}