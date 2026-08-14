import {APP_INITIALIZER, ApplicationConfig, LOCALE_ID} from '@angular/core';
import {provideRouter} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {registerLocaleData} from '@angular/common';
import localeRu from '@angular/common/locales/ru';
import {routes} from './app.routes';
import {authInterceptor} from './interceptor/auth-interceptor';
import {AuthService} from "./services/auth.service";

registerLocaleData(localeRu);

function initializeAuth(authService: AuthService): () => void {
  return () => authService.initialize();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAuth,
      deps: [AuthService],
      multi: true
    },
    {provide: LOCALE_ID, useValue: 'ru-RU'}
  ]
};
