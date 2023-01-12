package com.udacity.catpoint.service;

import com.udacity.catpoint.ImageService.service.FakeImageService;
import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.awt.image.BufferedImage;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {


    @Mock
    public SecurityRepository security;


    @Mock
    public FakeImageService imageService;

    SecurityService service;

    @BeforeEach
    void init(){
        service = new SecurityService(security, imageService);
    }


    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    void Sensor_Panding_Armed_isActive(ArmingStatus status) {
        Sensor sensor = new Sensor("door", SensorType.DOOR);
        lenient().when(security.getArmingStatus()).thenReturn(status);
        lenient().when(security.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, true);
        assertTrue(security.getAlarmStatus()==AlarmStatus.PENDING_ALARM);
    }

    @Test
    void Sensor_Activation_from_Pending_to_Alarm(){
        Sensor sensor = new Sensor("door", SensorType.DOOR);
        lenient().when(security.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        lenient().when(security.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        service.addSensor(sensor);
        sensor.setActive(true);
        service.handleSensorDeactivated();
        service.changeSensorActivationStatus(sensor, false);
        lenient().when(security.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        assertTrue(security.getAlarmStatus()==AlarmStatus.NO_ALARM);

    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME" , "ARMED_AWAY"})
    void  Changing_sensor_deactivation( ArmingStatus status) {
        Sensor sensor = new Sensor("door", SensorType.DOOR);
        lenient().when(security.getArmingStatus()).thenReturn(status);
        lenient().when(security.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        service.changeSensorActivationStatus(sensor, false);
        assertEquals(security.getAlarmStatus(),AlarmStatus.ALARM);
    }

    @Test
    void Deactivation_sensor_from_pending_to_noAlarm(){
        Sensor sensor = new Sensor("door", SensorType.DOOR);
        when(security.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, false);
        when(security.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        assertEquals(security.getAlarmStatus(),AlarmStatus.NO_ALARM);
    }


    @Test
    void Change_Sensor_from_pending_to_Alarm(){
        Sensor sensor = new Sensor("door", SensorType.DOOR);
        when(security.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(security.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, true);
        when(security.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        assertTrue(service.getAlarmStatus() == AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @CsvSource({"ALARM", "PENDING_ALARM", "NO_ALARM"})
    void Sensor_Inactive_NoChange_ToAlarm(AlarmStatus alarmStatus) {
        Sensor sensor = new Sensor("door", SensorType.DOOR);
        when(security.getAlarmStatus()).thenReturn(alarmStatus);
        service.changeSensorActivationStatus(sensor, false);
        assertTrue(service.getAlarmStatus() == alarmStatus);
        service.changeSensorActivationStatus(sensor, false);
        assertTrue(service.getAlarmStatus() == alarmStatus);

    }
    @Mock
    public Sensor ssensor;
    @Test
    void Sensor_inactive_ToActive() {
        Set<Sensor> sensors = new HashSet<Sensor>();
        sensors.add(ssensor);
        when(ssensor.getActive()).thenReturn(true);
        when(security.getSensors()).thenReturn(sensors);
        when(security.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        service.changeSensorActivationStatus(ssensor, false);
        assertTrue(service.getSensors().stream().anyMatch(sensor -> sensor.getActive()== true));
    }
    @Mock
    public BufferedImage image;
    @Test
    void Cat_founding_AlarmStatus(){
        Sensor sensor = new Sensor("door", SensorType.DOOR);
        Set<Sensor> sensors = new HashSet<Sensor>();
        sensors.add(sensor);
        lenient().when(security.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        lenient().when(security.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        lenient().when(security.getSensors()).thenReturn(sensors);
        lenient().when(imageService.imageContainsCat(image, 50.0f)).thenReturn(true);

        service.processImage(image);
        assertTrue(service.getAlarmStatus()== AlarmStatus.ALARM);
    }

    @Test
    void Cat_Notfound_NoAlarmStatus(){
      service.setArmingStatus(ArmingStatus.ARMED_HOME);
      when(imageService.imageContainsCat(image, 50.0f)).thenReturn(false);
      when(security.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
      service.processImage(image);
      Sensor sensor = new Sensor("test", SensorType.DOOR);
      service.changeSensorActivationStatus(sensor,true);
      assertTrue(service.getAlarmStatus() == AlarmStatus.NO_ALARM);
    }

    @Test
    void From_Disarmed_to_NoAlarm(){
        service.setArmingStatus(ArmingStatus.DISARMED);
        verify(security).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void If_Armed_rest_To_Inactive(){
        Sensor sensor = new Sensor("door", SensorType.DOOR);
        when(security.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        service.setArmingStatus(ArmingStatus.DISARMED);
        service.changeSensorActivationStatus(sensor, true);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        assertFalse(service.getSensors().stream().anyMatch(s -> s.getActive() == true));
    }

    @Test
    void If_ArmedHome_Found_Cat_set_Alarm(){
        when(security.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(image, 50.0f)).thenReturn(true);
        service.processImage(image);
        service.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(security, atLeast(1)).setAlarmStatus(AlarmStatus.ALARM);
    }
    EventListener listener;
    @Test
    void Add_and_remove_sensor(){

        Sensor sensor = new Sensor("door", SensorType.DOOR);
        service.addSensor(sensor);
        service.getSensors();
        service.removeSensor(sensor);
        service.addStatusListener((StatusListener) listener);
        service.removeStatusListener((StatusListener) listener);
        verify(security).addSensor(sensor);
        verify(security).getSensors();
        verify(security).removeSensor(sensor);
        when(security.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        service.handleSensorDeactivated();
    }




}
