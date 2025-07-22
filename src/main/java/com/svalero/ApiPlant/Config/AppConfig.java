package com.svalero.ApiPlant.Config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//es una clase para no tener que crear cada vez el objeto model mapper e instanciarlo

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}





/*

package com.svalero.ApiPlant.Config;

import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.PlantaInDto;
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // Evita el mapeo automático que confunde el idPlanta con otras IDs
        mapper.typeMap(PlantaInDto.class, Planta.class).addMappings(m -> {
            m.skip(Planta::setIdPlanta); // Esto evita el conflicto con idPlanta
        });

        return mapper;
    }
}
*/


