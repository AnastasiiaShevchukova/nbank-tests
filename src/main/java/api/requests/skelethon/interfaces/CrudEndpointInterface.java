package api.requests.skelethon.interfaces;

import api.models.BaseModel;

//Общие методы, свойственные конкретному типу ендпойнта
public interface CrudEndpointInterface {
    Object post(BaseModel model);

    Object get();

    Object update(BaseModel model);

    Object delete(long id);

}
