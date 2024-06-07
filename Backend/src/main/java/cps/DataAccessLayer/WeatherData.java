package cps.DataAccessLayer;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "Weather")
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Double temperature;
    private Double windSpeed;
    private Double windDirection;
    private Double sunMin;
    private Double cloudCoverage;
    private Double humidity;
    private Double rain;
    private Double solarRad;
    private Timestamp timestamp;
}
