package common.storage;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    /**
     * ThreadLocal - способ сделать SessionStorage потокобезопасным
     *
     * Каждый поток обращаясь к INSTANCE.get() получает свою копию (не оригинальный INSTANCE)
     *
     * Под капотом Map<Thread, SessionStorage>
     *     Когда мы обращаемся к этой мапе, мы получаем в соответствии со своим Thread свою SessionStorage
     */
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();

    // хранение аккаунтов и сумм депозитов
    private final HashMap<CreateUserRequest, CreateAccountResponse> userAccounts = new HashMap<>();
    private final HashMap<CreateUserRequest, Double> userDeposits = new HashMap<>();

    private SessionStorage(){};

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user : users) {
            INSTANCE.get().userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
    }

    /**
     * Возвращаем объект CreateUserRequest по его порядковому номеру в его списке созданных пользователей.
     * @param index Порядковый номер (начиная с 1, а не 0)
     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру
     */
    public static CreateUserRequest getUser(int index) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.keySet()).get(index - 1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.values()).get(number - 1);

    }

    public static UserSteps getSteps() {
        return getSteps(1);

    }

    public static void clear() {
        INSTANCE.get().userStepsMap.clear();
        INSTANCE.get().userAccounts.clear();
        INSTANCE.get().userDeposits.clear();
    }

    // метод для сохранения аккаунта пользователя
    public static void setUserAccount(CreateUserRequest user, CreateAccountResponse account) {
        INSTANCE.get().userAccounts.put(user, account);
    }

    // метод для сохранения суммы депозита
    public static void setUserDeposit(CreateUserRequest user, double depositAmount) {
        INSTANCE.get().userDeposits.put(user, depositAmount);
    }

    // Получение аккаунта пользователя
    public static CreateAccountResponse getUserAccount(int index) {
        CreateUserRequest user = getUser(index);
        return INSTANCE.get().userAccounts.get(user);
    }

    // Получение суммы депозита пользователя
    public static double getUserDeposit(int index) {
        CreateUserRequest user = getUser(index);
        return INSTANCE.get().userDeposits.get(user);
    }
}
