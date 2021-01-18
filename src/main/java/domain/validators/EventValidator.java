package domain.validators;

import domain.Eveniment;

public class EventValidator implements Validator<Eveniment> {
    @Override
    public void validate(Eveniment entity) throws ValidationException {
        String errors = "";
        if(entity.getTitlu().length() < 3)
            errors+="Title too short\n";
        if(entity.getTitlu().length() > 100)
            errors+="Title too long\n";

        if(!errors.equals(""))
            throw new ValidationException(errors);
    }
}
