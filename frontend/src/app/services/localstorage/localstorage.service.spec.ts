import {TestBed} from '@angular/core/testing';

import {BROWSER_STORAGE, LocalStorageService} from './local-storage.service';

describe('LocalstorageService', () => {
  let service: LocalStorageService;
  let storageMock: jasmine.SpyObj<Storage>;

  beforeEach(() => {
    storageMock = jasmine.createSpyObj<Storage>('Storage', [
      'getItem', 'setItem'
    ])
    TestBed.configureTestingModule({
      providers: [
        LocalStorageService,
        {provide: BROWSER_STORAGE, useValue: storageMock}
      ]
    });
    service = TestBed.inject(LocalStorageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call setItem on storage', () => {
    service.set('key', 'value')
    expect(storageMock.setItem).toHaveBeenCalledWith('key', 'value')
  })

  it('should call getItem on storage', () => {
    storageMock.getItem.and.returnValue('value')
    const result = service.get('key')
    expect(storageMock.getItem).toHaveBeenCalledWith('key')
    expect(result).toBe('value')
  })
});
