package org.systemhotelowy.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.systemhotelowy.dto.ReservationRequest;
import org.systemhotelowy.model.Reservation;
import org.systemhotelowy.model.ReservationStatus;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.service.ReservationService;
import org.systemhotelowy.utils.NotificationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReservationFormDialog extends Dialog {

    private final ReservationService reservationService;
    private final List<Room> rooms;
    private final Runnable onSuccess;
    private final Reservation reservation;
    private final boolean isNew;

    private TextField guestNameField;
    private TextField guestEmailField;
    private TextField phoneField;
    private ComboBox<Room> roomField;
    private DatePicker checkInField;
    private DatePicker checkOutField;
    private IntegerField guestsField;
    private NumberField priceField;
    private ComboBox<ReservationStatus> statusField;
    private TextArea notesField;

    public ReservationFormDialog(Reservation reservation, List<Room> rooms, ReservationService reservationService, Runnable onSuccess) {
        this.reservation = reservation != null ? reservation : new Reservation();
        this.isNew = (reservation == null || reservation.getId() == null);
        this.rooms = rooms;
        this.reservationService = reservationService;
        this.onSuccess = onSuccess;

        setHeaderTitle(isNew ? "Dodaj rezerwację" : "Edytuj rezerwację");
        setWidth("600px");

        createForm();
        createFooter();
    }

    // Constructor for pre-filling data for new reservation
    public ReservationFormDialog(Room preselectedRoom, LocalDate preselectedDay, List<Room> rooms, ReservationService reservationService, Runnable onSuccess) {
        this(null, rooms, reservationService, onSuccess);
        if (preselectedRoom != null) roomField.setValue(preselectedRoom);
        if (preselectedDay != null) {
            checkInField.setValue(preselectedDay);
            checkOutField.setValue(preselectedDay.plusDays(1));
        }
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();

        guestNameField = new TextField("Imię i nazwisko gościa");
        if (!isNew) guestNameField.setValue(reservation.getGuestName());
        guestNameField.setRequiredIndicatorVisible(true);

        guestEmailField = new TextField("Email gościa");
        if (!isNew && reservation.getGuestEmail() != null) guestEmailField.setValue(reservation.getGuestEmail());

        phoneField = new TextField("Numer telefonu");
        if (!isNew && reservation.getGuestPhone() != null) phoneField.setValue(reservation.getGuestPhone());
        phoneField.setRequiredIndicatorVisible(true);

        roomField = new ComboBox<>("Pokój");
        roomField.setItems(rooms);
        roomField.setItemLabelGenerator(room -> room.getNumber() + " (max: " + room.getCapacity() + " osób)");
        roomField.setRequiredIndicatorVisible(true);
        if (!isNew) roomField.setValue(reservation.getRoom());

        checkInField = new DatePicker("Check-in");
        checkInField.setRequiredIndicatorVisible(true);
        if (!isNew) checkInField.setValue(reservation.getCheckInDate());

        checkOutField = new DatePicker("Check-out");
        checkOutField.setRequiredIndicatorVisible(true);
        if (!isNew) checkOutField.setValue(reservation.getCheckOutDate());

        guestsField = new IntegerField("Liczba gości");
        guestsField.setValue(isNew ? 1 : reservation.getNumberOfGuests());
        guestsField.setMin(1);
        guestsField.setRequiredIndicatorVisible(true);

        priceField = new NumberField("Całkowita cena (PLN)");
        priceField.setRequiredIndicatorVisible(true);
        if (!isNew && reservation.getTotalPrice() != null) {
            priceField.setValue(reservation.getTotalPrice().doubleValue());
            // In edit mode, price might be read-only or editable depending on requirements.
            priceField.setReadOnly(true);
        }
        priceField.setSuffixComponent(new Span("PLN"));

        statusField = new ComboBox<>("Status");
        statusField.setItems(ReservationStatus.values());
        statusField.setValue(isNew ? ReservationStatus.PENDING : reservation.getStatus());
        statusField.setItemLabelGenerator(this::formatStatus);

        notesField = new TextArea("Uwagi");
        if (!isNew && reservation.getNotes() != null) notesField.setValue(reservation.getNotes());

        formLayout.add(guestNameField, guestEmailField, phoneField, roomField,
                      checkInField, checkOutField, guestsField, priceField, statusField, notesField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        add(formLayout);
    }

    private void createFooter() {
        Button cancelBtn = new Button("Anuluj", e -> close());
        Button saveBtn = new Button(isNew ? "Zapisz" : "Zapisz zmiany", e -> save());
        saveBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);

        getFooter().add(cancelBtn, saveBtn);
    }

    private void save() {
        if (roomField.isEmpty() || checkInField.isEmpty() || checkOutField.isEmpty() ||
            guestNameField.isEmpty() || phoneField.isEmpty() || priceField.isEmpty()) {
            NotificationUtils.showError("Wypełnij wszystkie wymagane pola");
            return;
        }

        try {
            ReservationRequest request = new ReservationRequest();
            request.setRoomId(roomField.getValue().getId());
            request.setCheckInDate(checkInField.getValue());
            request.setCheckOutDate(checkOutField.getValue());
            request.setGuestName(guestNameField.getValue());
            request.setGuestEmail(guestEmailField.getValue());
            request.setGuestPhone(phoneField.getValue());
            request.setNumberOfGuests(guestsField.getValue());
            request.setStatus(statusField.getValue());
            request.setNotes(notesField.getValue());
            if (isNew) {
                request.setTotalPrice(BigDecimal.valueOf(priceField.getValue()));
                reservationService.create(request);
                NotificationUtils.showSuccess("Rezerwacja dodana pomyślnie");
            } else {
                // Price is read-only in edit, but we should pass it if needed, or keep original
                if (priceField.getValue() != null) {
                    request.setTotalPrice(BigDecimal.valueOf(priceField.getValue()));
                } else {
                    request.setTotalPrice(reservation.getTotalPrice());
                }
                reservationService.update(reservation.getId(), request);
                NotificationUtils.showSuccess("Rezerwacja zaktualizowana");
            }

            if (onSuccess != null) onSuccess.run();
            close();

        } catch (Exception ex) {
            NotificationUtils.showError("Błąd: " + ex.getMessage());
        }
    }

    private String formatStatus(ReservationStatus status) {
        if (status == null) return "";
        return switch (status) {
            case CONFIRMED -> "Potwierdzona";
            case PENDING -> "Oczekująca";
            case CANCELLED -> "Anulowana";
            case CHECKED_IN -> "Zameldowany";
            case CHECKED_OUT -> "Wymeldowany";
            default -> status.name();
        };
    }
}
