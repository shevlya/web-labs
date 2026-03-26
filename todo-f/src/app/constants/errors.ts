export const ERROR_MESSAGES = {
  AUTH: {
    INVALID_CREDENTIALS: 'Неверный логин или пароль',
    SERVER_ERROR: 'Ошибка сервера, попробуйте позже',
    TOKEN_INVALID: 'Ошибка декодирования токена',
    USER_NOT_FOUND: 'Не удалось определить пользователя'
  },
  
  TASK: {
    LOAD_FAILED: 'Не удалось загрузить задачу',
    SAVE_FAILED: 'Ошибка сохранения',
    DELETE_FAILED: 'Не удалось удалить задачу',
    LIST_LOAD_FAILED: 'Ошибка загрузки задач'
  }
} as const;
