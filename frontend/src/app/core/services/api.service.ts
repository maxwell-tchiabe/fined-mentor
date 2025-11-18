import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export abstract class ApiService {
  protected readonly apiUrl = environment.apiUrl;

  constructor(protected http: HttpClient) {}
}