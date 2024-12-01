package org.luiz.sa.waterdatacollector;

import org.luiz.sa.waterdatacollector.model.AverageDeviceReadValues;
import org.luiz.sa.waterdatacollector.model.DeviceReadValues;
import org.luiz.sa.waterdatacollector.model.WaterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WaterDataCollectorService {
    @Autowired
    private WaterDataCollectorRepository waterDataCollectorRepository;
    private long hourInMilliSeconds = 60*60*1000; // Equation to get the equivalent of an hour in milliseconds

    public void saveData(WaterData waterData) {
        waterDataCollectorRepository.save(waterData);
    }

    private WaterData loadDataById(Long waterDataId) {
        return waterDataCollectorRepository.getReferenceById(waterDataId);
    }

    private List<WaterData> loadAllDataByDeviceId(String deviceId) {
        return waterDataCollectorRepository.findByDeviceId(deviceId);
    }

    private List<WaterData> loadAllData() {
        return waterDataCollectorRepository.findAll();
    }

    private void deleteData(Long waterDataId) {
        waterDataCollectorRepository.deleteById(waterDataId);
    }

    public WaterData loadLastReading(String deviceId) {
        return waterDataCollectorRepository.findLastReadByDeviceId(deviceId);
    }

    private List<WaterData> loadLastHourReadings(String deviceId) {
        long timestamp = loadLastReading(deviceId).getTimestamp();
        timestamp -= this.hourInMilliSeconds;
        return waterDataCollectorRepository.findDeviceIdLastHourReads(deviceId, timestamp);
    }

    public DeviceReadValues loadAllSensorsValuesByDeviceId(String deviceId) {
        long timestamp = loadLastReading(deviceId).getTimestamp();
        timestamp -= this.hourInMilliSeconds;
        DeviceReadValues recentValues = new DeviceReadValues();
        recentValues.setPh(waterDataCollectorRepository.getPhSensorsValuesByDeviceId(deviceId, timestamp));
        recentValues.setTemperature(waterDataCollectorRepository.getTemperatureSensorsValuesByDeviceId(deviceId, timestamp));
        recentValues.setTds(waterDataCollectorRepository.getTdsSensorsValuesByDeviceId(deviceId, timestamp));

        return recentValues;
    }

    public AverageDeviceReadValues averageValueLastHourReads(String deviceId) {
        DeviceReadValues recentValues = loadAllSensorsValuesByDeviceId(deviceId);
        AverageDeviceReadValues averageValues = new AverageDeviceReadValues();

        float averagePh = 0F;
        float averageTemperature = 0F;
        int averageTds = 0;
        int listSize = recentValues.getPh().size();

        for (int i = 0; i < listSize; i++) {
            averagePh += recentValues.getPh().get(i);
            averageTemperature += recentValues.getTemperature().get(i);
            averageTds += recentValues.getTds().get(i);
        }

        averageValues.setPh(averagePh / listSize);
        averageValues.setTemperature(averageTemperature / listSize);
        averageValues.setTds(averageTds / listSize);

        return averageValues;
    }
}
