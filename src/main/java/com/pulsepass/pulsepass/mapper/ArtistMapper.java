package com.pulsepass.pulsepass.mapper;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.dto.response.ArtistResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArtistMapper {

    ArtistResponse toResponse(Artist artist);

    List<ArtistResponse> toResponseList(List<Artist> artists);
}