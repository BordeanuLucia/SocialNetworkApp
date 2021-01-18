package domain.validators;

import domain.Prietenie;

public class PrietenieValidator implements Validator<Prietenie> {

    @Override
    public void validate(Prietenie entity) throws ValidationException {
        String errors = "";
        if(entity.getId().getRight().equals(entity.getId().getLeft()))
            errors+="You cannot befriend yourself";
        if(!errors.isEmpty())
            throw new ValidationException(errors);
    }
}