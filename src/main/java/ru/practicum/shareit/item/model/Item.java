package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String description;

    private Boolean isAvailable;

    private Long ownerId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private List<Comment> comments = new ArrayList<>();

    private transient Booking lastBooking;

    private transient Booking nextBooking;
}
