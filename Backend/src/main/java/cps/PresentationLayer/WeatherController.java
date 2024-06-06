package cps.PresentationLayer;

import cps.BuisnessLogicLayer.WeatherService;
import cps.DataAccessLayer.WeatherData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@RestController
@RequestMapping("api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/time")
    public String GetTimeData() {
        return weatherService.getDateTime();
    }

    @GetMapping("/time/all")
    public ResponseEntity<Timestamp[]> GetAllTimeData() {
        return ResponseEntity.ok(weatherService.getAllTimestamps());
    }

    @PostMapping("/update")
    public void StoreValueDB() {
        weatherService.storeValuesInDB();
    }

    @GetMapping("/")
    public WeatherData GetWeatherData() {
        return weatherService.getLatestValueFromDB();
    }

    @GetMapping("/time/{timestampInput}")
    public WeatherData GetWeatherDataSpecific(@PathVariable String timestampInput) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
        try {
            Timestamp timestamp = new Timestamp(simpleDateFormat.parse(timestampInput).getTime());
            return weatherService.getValueFromTimestamp(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/overall")
    public double getOverallWeather() {
        return weatherService.calculateOverallWeather();
    }
}
