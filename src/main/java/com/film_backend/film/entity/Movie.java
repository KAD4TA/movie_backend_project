package com.film_backend.film.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.film_backend.film.enums.Genre;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String description;
    
    @ElementCollection(targetClass = Genre.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "movie_genres",
        joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "genre")
    private Set<Genre> genres = new HashSet<Genre>();

    @Column
    private String posterUrl;

    @Column
    private String videoUrl;
    
    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private Integer releaseYear;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;
    
    private Double averageRating; 

    public void updateAverageRating(List<Comment> comments) {
        if (comments.isEmpty()) {
            this.averageRating = null;
        } else {
            double avg = comments.stream()
                    .mapToInt(Comment::getRate)
                    .average()
                    .orElse(0);
            this.averageRating = avg;
        }
    }

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}