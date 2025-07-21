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
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Mapeo personalizado Planta -> PlantaOutDto para coger solo los IDs
        modelMapper.typeMap(Planta.class, PlantaOutDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getCuidado().getId_cuidado(), PlantaOutDto::setCuidadoId);
            mapper.map(src -> src.getCategoria().getId_categoria(), PlantaOutDto::setCategoriaId);
        });

        return modelMapper;
    }
}
*/
