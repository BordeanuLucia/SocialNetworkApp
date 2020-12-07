package SocialNetwork.domain.validators;

import SocialNetwork.domain.Utilizator;

public class UtilizatorValidator implements Validator<Utilizator> {
    @Override
    public void validate(Utilizator entity) throws ValidationException {
        String errors = "";
        if(entity.getLastName().length() < 3 || entity.getFirstName().length() < 3)
            errors+="Name too short\n";
        if(entity.getLastName().length() > 25 || entity.getFirstName().length() > 25)
            errors+="Name too long\n";
        if(!entity.getFirstName().chars().allMatch(Character::isLetter) || !entity.getLastName().chars().allMatch(Character::isLetter))
            errors+="Name should have only letters\n";
        if(entity.getPassword().length() <5)
            errors+="Password too weak";
        if(!errors.equals(""))
            throw new ValidationException(errors);
    }
}