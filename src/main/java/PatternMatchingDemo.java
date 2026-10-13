// Alle Werte sind frei erfunden und keine medizinische Beratung.

import java.util.List;
import java.util.Objects;

enum Drug { PARACETAMOL, CAFFEINE, IBUPROFEN }

sealed interface Dosage permits Tablet, Infusion { }

record Tablet(int morning, int midday, int evening) implements Dosage {
    Tablet {
        if (morning < 0 || midday < 0 || evening < 0) {
            throw new IllegalArgumentException("negative number of tablets");
        }
    }
}

record Infusion(int speed, int duration) implements Dosage { }

record Medication(Drug drug, Dosage dosage) {
    Medication {
        Objects.requireNonNull(drug);
        Objects.requireNonNull(dosage);
    }
}

// Schritt 0: klassische Fallunterscheidung mit instanceof und Cast
String formatDosageClassic(Dosage dosage) {
    if (dosage instanceof Tablet) {
        Tablet tablet = (Tablet) dosage;
        return "%d-%d-%d".formatted(tablet.morning(), tablet.midday(), tablet.evening());
    }
    if (dosage instanceof Infusion) {
        Infusion infusion = (Infusion) dosage;
        return "%d ml/h for %d h".formatted(infusion.speed(), infusion.duration());
    }
    throw new IllegalArgumentException("Unknown dosage: " + dosage);
}

// Schritt 1-3: Type Patterns, switch ohne default (sealed), Record Patterns
String formatDosage(Dosage dosage) {
    return switch (dosage) {
        case Tablet(int morning, int midday, int evening) ->
                "%d-%d-%d".formatted(morning, midday, evening);
        case Infusion(int speed, int duration) ->
                "%d ml/h for %d h".formatted(speed, duration);
    };
}

// Schritt 4: verschachtelte Record Patterns
String describe(Medication medication) {
    return switch (medication) {
        case Medication(var drug, Tablet(int morning, int midday, int evening)) ->
                "%s: %d-%d-%d".formatted(drug, morning, midday, evening);
        case Medication(var drug, Infusion(int speed, int duration)) ->
                "%s: %d ml/h for %d h".formatted(drug, speed, duration);
    };
}

// Schritt 5: Guards, null und Reihenfolge (spezielle Fälle vor dem allgemeinen)
String warning(Medication medication) {
    return switch (medication) {
        case null -> "no medication";
        case Medication(var drug, Infusion(_, int duration)) when duration > 8 ->
                drug + ": long-running infusion";
        case Medication(var drug, Tablet(int morning, int midday, int evening))
                when morning + midday + evening > 6 ->
                drug + ": many tablets";
        case Medication _ -> "no warning";
    };
}

// Schritt 5: Unnamed Patterns
String administrationType(Dosage dosage) {
    return switch (dosage) {
        case Tablet(_, _, _) -> "oral";
        case Infusion _ -> "infusion";
    };
}

// Schritt 6: neue Operation auf stabilen Daten, ohne die Typen anzufassen
int dailyAmount(Dosage dosage) {
    return switch (dosage) {
        case Tablet(int morning, int midday, int evening) -> morning + midday + evening;
        case Infusion(int speed, int duration) -> speed * duration;
    };
}

// Bonus (Preview): Primitive Type Patterns im switch
String volumeClass(long milliliters) {
    return switch (milliliters) {
        case 0L -> "none";
        case int ml when ml < 500 -> "small (%d ml)".formatted(ml);
        case int ml -> "large (%d ml)".formatted(ml);
        case long ml -> "huge (%d ml)".formatted(ml);
    };
}

void main() {
    var plan = List.of(
            new Medication(Drug.PARACETAMOL, new Tablet(1, 1, 1)),
            new Medication(Drug.IBUPROFEN, new Tablet(3, 3, 2)),
            new Medication(Drug.CAFFEINE, new Infusion(50, 12)));

    IO.println(formatDosageClassic(plan.getFirst().dosage()));
    IO.println(formatDosage(plan.getFirst().dosage()));

    for (var medication : plan) {
        IO.println(describe(medication));
        IO.println("  " + warning(medication));
        IO.println("  " + administrationType(medication.dosage()));
        IO.println("  " + volumeClass(dailyAmount(medication.dosage())));
    }

    IO.println(warning(null));
    IO.println(volumeClass(Long.MAX_VALUE));
}
