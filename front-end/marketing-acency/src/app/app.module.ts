import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { NavbarComponent } from './feature-modules/layout/navbar/navbar.component';
import { LayoutModule } from './feature-modules/layout/layout.module';
import { FooterComponent } from './feature-modules/layout/footer/footer.component';
import { RouterModule } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from './infrastructure/routing/app-routing.module';
import { HomeComponent } from './feature-modules/layout/home/home.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AuthModule } from './infrastructure/auth/auth.module';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { JwtInterceptor } from '@auth0/angular-jwt';
import { AuthService } from './infrastructure/auth/auth.service';
import { TokenInterceptor } from './infrastructure/auth/interceptor/TokenInterceptor';
import { EmployeeModule } from './feature-modules/employee/employee.module';
import { RegistrationRequestComponent } from './feature-modules/administrator/registration-request/registration-request.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@NgModule({
  declarations: [
    AppComponent,
    RegistrationRequestComponent,
  ],
  imports: [
    BrowserModule,
    LayoutModule,
    RouterModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    EmployeeModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule 
  ],
  providers: [ 
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true,     
    },
    
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
