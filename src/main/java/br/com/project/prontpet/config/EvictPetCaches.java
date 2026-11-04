package br.com.project.prontpet.config;


import org.springframework.cache.annotation.CacheEvict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@CacheEvict(value = {"pets", "breeds", "dates", "species", "names"}, allEntries = true)
public @interface EvictPetCaches {
}
