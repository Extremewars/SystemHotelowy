package org.systemhotelowy.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.model.RoomType;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.utils.NotificationUtils;

public class RoomFormDialog extends Dialog {

    private final RoomService roomService;
    private final Runnable onSuccess;
    private final Room room;
    private final boolean isNew;

    private TextField numberField;
    private IntegerField floorField;
    private ComboBox<RoomType> typeCombo;
    private ComboBox<RoomStatus> statusCombo;
    private IntegerField capacityField;

    public RoomFormDialog(Room room, RoomService roomService, Runnable onSuccess) {
        this.room = room;
        this.isNew = (room.getId() == null);
        this.roomService = roomService;
        this.onSuccess = onSuccess;

        setHeaderTitle(isNew ? "Nowy Pokój" : "Edycja Pokoju " + room.getNumber());
        setWidth("500px");

        createForm();
        createFooter();
    }

    /**
     * Tryb tylko do zmiany statusu (ukrywa inne pola).
     */
    public void openStatusOnlyMode() {
        numberField.setReadOnly(true);
        floorField.setVisible(false);
        typeCombo.setVisible(false);
        capacityField.setVisible(false);
        setHeaderTitle("Zmień status: " + room.getNumber());
        open();
    }

    private void createForm() {
        FormLayout form = new FormLayout();

        numberField = new TextField("Numer");
        numberField.setValue(room.getNumber() != null ? room.getNumber() : "");
        numberField.setRequired(true);

        floorField = new IntegerField("Piętro");
        floorField.setValue(room.getFloor() != null ? room.getFloor() : 1);

        typeCombo = new ComboBox<>("Typ");
        typeCombo.setItems(RoomType.values());
        typeCombo.setItemLabelGenerator(this::translateRoomType);
        typeCombo.setValue(room.getType() != null ? room.getType() : RoomType.SINGLE);

        statusCombo = new ComboBox<>("Status");
        statusCombo.setItems(RoomStatus.values());
        statusCombo.setItemLabelGenerator(this::translateRoomStatus);
        statusCombo.setValue(room.getRoomStatus() != null ? room.getRoomStatus() : RoomStatus.DIRTY);

        capacityField = new IntegerField("Pojemność");
        capacityField.setValue(room.getCapacity() != null ? room.getCapacity() : 2);
        capacityField.setMin(1);

        form.add(numberField, floorField, typeCombo, statusCombo, capacityField);
        add(form);
    }

    private void createFooter() {
        Button cancelBtn = new Button("Anuluj", e -> close());
        Button saveBtn = new Button("Zapisz", e -> save());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(cancelBtn, saveBtn);
    }

    private void save() {
        if (numberField.isEmpty()) {
            NotificationUtils.showError("Numer jest wymagany");
            return;
        }

        try {
            room.setNumber(numberField.getValue());
            room.setFloor(floorField.getValue());
            room.setType(typeCombo.getValue());
            room.setRoomStatus(statusCombo.getValue());
            room.setCapacity(capacityField.getValue());

            if (isNew) {
                roomService.create(room);
            } else {
                roomService.update(room);
            }

            NotificationUtils.showSuccess("Zapisano pomyślnie");
            if (onSuccess != null) onSuccess.run();
            close();

        } catch (Exception e) {
            NotificationUtils.showError("Błąd: " + e.getMessage());
        }
    }

    private String translateRoomType(RoomType type) {
        if (type == null) return "";
        return switch (type) {
            case SINGLE -> "Pojedynczy";
            case DOUBLE -> "Podwójny";
            case SUITE -> "Apartament";
            case OTHER -> "Inny";
            default -> type.name();
        };
    }

    private String translateRoomStatus(RoomStatus status) {
        if (status == null) return "";
        return switch (status) {
            case READY -> "Gotowy";
            case DIRTY -> "Brudny";
            case CLEANING -> "Sprzątanie";
            case IN_MAINTENANCE -> "Konserwacja";
            case OUT_OF_ORDER -> "Awaria";
            default -> status.name();
        };
    }
}
